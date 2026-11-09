export interface EmailRequest {
  to: string;
  subject: string;
  message: string;
}

export type EmailStatus = 'SENT' | 'FAILED';

export interface EmailRecord {
  id: number;
  to: string;
  subject: string;
  message: string;
  status: EmailStatus;
  detail: string;
  sentAt: string;
}

export interface EmailResponse {
  success: boolean;
  message: string;
  email: EmailRecord;
}

export interface EmailTemplate {
  id: number;
  name: string;
  subject: string;
  body: string;
}
