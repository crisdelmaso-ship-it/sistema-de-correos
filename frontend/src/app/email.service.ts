import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './api.config';
import { EmailRecord, EmailRequest, EmailResponse, EmailTemplate } from './models';

@Injectable({ providedIn: 'root' })
export class EmailService {
  private readonly http = inject(HttpClient);

  send(request: EmailRequest): Observable<EmailResponse> {
    return this.http.post<EmailResponse>(`${API_URL}/emails/send`, request);
  }

  history(): Observable<EmailRecord[]> {
    return this.http.get<EmailRecord[]>(`${API_URL}/emails`);
  }

  templates(): Observable<EmailTemplate[]> {
    return this.http.get<EmailTemplate[]>(`${API_URL}/templates`);
  }

  createTemplate(template: Omit<EmailTemplate, 'id'>): Observable<EmailTemplate> {
    return this.http.post<EmailTemplate>(`${API_URL}/templates`, template);
  }
}
