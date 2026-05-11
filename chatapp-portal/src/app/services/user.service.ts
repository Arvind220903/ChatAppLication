import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';

export interface User {
  userId?: number;
  userName: string;
  userEmail: string;
  password?: string;
  userbio?: string;
  userProfile?: string;
  follower?: number[];
  following?: number[];
  posts?: any[];
  postCount?: number;
  followerCount?: number;
  followingCount?: number;
  unseenNoti?: number;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = 'http://localhost:8080';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  public token: string | null = null;

  constructor(private http: HttpClient) {
    const savedToken = localStorage.getItem('chatapp_token');
    const savedUser = localStorage.getItem('chatapp_user');
    if (savedToken) this.token = savedToken;
    if (savedUser) this.currentUserSubject.next(JSON.parse(savedUser));
  }

  private get authHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${this.token || ''}`,
      'Autherization': `Bearer ${this.token || ''}`,
      'token': this.token || ''
    });
  }

  register(user: User): Observable<string> {
    return this.http.post(`${this.base}/user/register`, user, { responseType: 'text' });
  }

  login(email: string, password: string): Observable<string> {
    return this.http.post(`${this.base}/user/login`, { userEmail: email, password }, { responseType: 'text' }).pipe(
      tap(token => {
        this.token = token;
        localStorage.setItem('chatapp_token', token);
      })
    );
  }

  getFollowing(email?: string): Observable<User[]> {
    const url = email ? `${this.base}/user/followings/${email}` : `${this.base}/user/followings`;
    return this.http.get<User[]>(url, { headers: this.authHeaders });
  }

  getFollowers(email?: string): Observable<User[]> {
    const url = email ? `${this.base}/user/followers/${email}` : `${this.base}/user/followers`;
    return this.http.get<User[]>(url, { headers: this.authHeaders });
  }

  searchUser(username: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/user/searchuser/${username}`, { headers: this.authHeaders });
  }

  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.base}/user/getprofile`, { headers: this.authHeaders }).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        localStorage.setItem('chatapp_user', JSON.stringify(user));
      })
    );
  }

  logout() {
    this.token = null;
    this.currentUserSubject.next(null);
    localStorage.removeItem('chatapp_token');
    localStorage.removeItem('chatapp_user');
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getNotifications(): Observable<any[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.token || ''}`,
      'Autherization': `Bearer ${this.token || ''}`
    });
    return this.http.get<any[]>(`${this.base}/user/notifications`, { headers });
  }

  getProfileByUsername(username: string): Observable<User> {
    return this.http.get<User>(`${this.base}/user/getprofilebyusername/${username}`, { headers: this.authHeaders });
  }

  followUser(userId: number): Observable<string> {
    // Backend header is misspelled "Autherization" — must match exactly
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.token || ''}`,
      'Autherization': `Bearer ${this.token || ''}`
    });
    return this.http.post(
      `${this.base}/user/follow?Follower=${userId}`, null,
      { headers, responseType: 'text' }
    );
  }

  getCommentedPostsByUser(email: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/comments/getcommentedpostotheruser/${email}`, { headers: this.authHeaders });
  }

  updateProfile(user: User): Observable<User> {
    return this.http.put<User>(`${this.base}/user/editprofile`, user, { headers: this.authHeaders }).pipe(
      tap(updated => {
        const current = this.currentUser;
        if (current && updated.userId === current.userId) {
          this.currentUserSubject.next(updated);
          localStorage.setItem('chatapp_user', JSON.stringify(updated));
        }
      })
    );
  }

  clearNotifications(): Observable<string> {
    return this.http.put(`${this.base}/user/resetnotifications`, null, { headers: this.authHeaders, responseType: 'text' }).pipe(
      tap(() => {
        const current = this.currentUser;
        if (current) {
          this.updateLocalUser({ ...current, unseenNoti: 0 });
        }
      })
    );
  }

  updateLocalUser(user: User) {
    this.currentUserSubject.next(user);
    localStorage.setItem('chatapp_user', JSON.stringify(user));
  }
}
