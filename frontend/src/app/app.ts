import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EmailService } from './email.service';
import { EmailRecord, EmailTemplate } from './models';

type Alert = { type: 'success' | 'error'; text: string };

@Component({
  selector: 'app-root',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly emailService = inject(EmailService);

  protected readonly templates = signal<EmailTemplate[]>([]);
  protected readonly history = signal<EmailRecord[]>([]);
  protected readonly sending = signal(false);
  protected readonly alert = signal<Alert | null>(null);
  protected readonly selectedTemplate = signal<number | null>(null);

  protected readonly sentCount = computed(() => this.history().filter(e => e.status === 'SENT').length);
  protected readonly failedCount = computed(() => this.history().filter(e => e.status === 'FAILED').length);

  private readonly avatarColors = [
    'linear-gradient(135deg, #8b5cf6, #6366f1)',
    'linear-gradient(135deg, #ec4899, #f43f5e)',
    'linear-gradient(135deg, #f97316, #f59e0b)',
    'linear-gradient(135deg, #10b981, #06b6d4)',
    'linear-gradient(135deg, #3b82f6, #0ea5e9)'
  ];

  protected readonly form = this.fb.nonNullable.group({
    to: ['', [Validators.required, Validators.email]],
    subject: ['', [Validators.required, Validators.maxLength(150)]],
    message: ['', [Validators.required, Validators.maxLength(10000)]]
  });

  ngOnInit(): void {
    this.emailService.templates().subscribe({
      next: t => this.templates.set(t),
      error: () => this.showError('No se pudieron cargar las plantillas. ¿Está corriendo la API?')
    });
    this.loadHistory();
  }

  protected applyTemplate(template: EmailTemplate): void {
    this.selectedTemplate.set(template.id);
    this.form.patchValue({ subject: template.subject, message: template.body });
  }

  protected avatarColor(email: string): string {
    const hash = [...email].reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
    return this.avatarColors[hash % this.avatarColors.length];
  }

  protected send(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.sending.set(true);
    this.alert.set(null);

    this.emailService.send(this.form.getRawValue()).subscribe({
      next: res => {
        this.alert.set({ type: 'success', text: res.message });
        this.form.reset();
        this.selectedTemplate.set(null);
        this.sending.set(false);
        this.loadHistory();
      },
      error: (err: HttpErrorResponse) => {
        this.showError(this.extractError(err));
        this.sending.set(false);
        this.loadHistory();
      }
    });
  }

  protected invalid(field: 'to' | 'subject' | 'message'): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }

  private loadHistory(): void {
    this.emailService.history().subscribe({
      next: h => this.history.set(h),
      error: () => {}
    });
  }

  private showError(text: string): void {
    this.alert.set({ type: 'error', text });
  }

  private extractError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'No hay conexión con la API.';
    }
    const body = err.error;
    if (body?.errors) {
      return Object.values(body.errors).join(' · ');
    }
    return body?.message ?? 'Error al enviar el correo.';
  }
}
