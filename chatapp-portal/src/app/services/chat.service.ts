import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserService } from './user.service';

export interface Message {
  msgId?: number;
  msg: string;
  sender: number;
  receiver: number;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8080/messages';

  constructor(private http: HttpClient, private userService: UserService) { }

  private get authHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${this.userService.token || ''}`,
      'token': this.userService.token || ''
    });
  }

  getContacts(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/contacts`, { headers: this.authHeaders });
  }

  getChatHistory(userId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/history/${userId}`, { headers: this.authHeaders });
  }

  sendMessage(receiverId: number, msg: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/send/${receiverId}`, msg, {
      headers: this.authHeaders,
      responseType: 'text'
    });
  }
}
