import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserService } from './user.service';

export interface Post {
  postId?: number;
  title: string;
  user: number;
  userName?: string;
  likeCount?: number;
  createdAt?: string;
  comments?: any[];
  showCommentBox?: boolean;
  newCommentText?: string;
  likedByUser?: boolean;       // mapped from backend isLikedByUser()
  saveByuser?: boolean;        // exact backend field name from isSaveByuser()
  savedByUser?: boolean;       // frontend working field (normalised from saveByuser)
  displayTime?: string;
  tags?: string[];
  confirmDelete?: boolean;
  latitude?: number;
  longitude?: number;
}

@Injectable({ providedIn: 'root' })
export class PostService {
  private base = 'http://localhost:8080';

  constructor(private http: HttpClient, private userService: UserService) { }

  private get authHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${this.userService.token || ''}`,
      'token': this.userService.token || ''
    });
  }

  getFeed(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/posts/feed`, { headers: this.authHeaders });
  }

  createPost(post: Post): Observable<Post> {
    return this.http.post<Post>(`${this.base}/posts/createpost`, post, { headers: this.authHeaders });
  }

  likePost(postId: number): Observable<string> {
    return this.http.post(`${this.base}/likes/like/${postId}`, null,
      { headers: this.authHeaders, responseType: 'text' });
  }

  addComment(postId: number, userId: number, text: string): Observable<any[]> {
    const payload = { postId, userId, comment: text };
    return this.http.post<any[]>(`${this.base}/comments/addcomment`, payload,
      { headers: this.authHeaders });
  }

  savePost(postId: number): Observable<string> {
    return this.http.post(`${this.base}/posts/saved/${postId}`, null,
      { headers: this.authHeaders, responseType: 'text' });
  }

  getSavedPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/posts/savedposts`, { headers: this.authHeaders });
  }

  getLikedPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/likes/getlikeposts`, { headers: this.authHeaders });
  }

  getLikes(postId: number): Observable<number> {
    return this.http.get<number>(`${this.base}/likes/getLikes/${postId}`, { headers: this.authHeaders });
  }

  getCommentedPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/comments/getpostsbycomments`, { headers: this.authHeaders });
  }

  getTrending(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/posts/trending`, { headers: this.authHeaders });
  }

  getPostsByUser(userId: number): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/posts/postbyuser/${userId}`, { headers: this.authHeaders });
  }

  deletePost(postId: number, userId: number): Observable<string> {
    return this.http.put(`${this.base}/posts/delete?postid=${postId}&userId=${userId}`, null,
      { headers: this.authHeaders, responseType: 'text' });
  }

  findTags(keyword: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/tags/findtags?tag=${encodeURIComponent(keyword)}`, { headers: this.authHeaders });
  }

  getTrendingTags(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/tags/trendingTags`, { headers: this.authHeaders });
  }

  getPostsByTag(tag: string): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/tags/getpostsbytag?tag=${encodeURIComponent(tag)}`, { headers: this.authHeaders });
  }

  getRegionPosts(lat: number, lng: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/posts/region?lat=${lat}&lng=${lng}`, { headers: this.authHeaders });
  }
}
