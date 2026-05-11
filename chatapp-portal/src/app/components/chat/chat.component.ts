import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatService, Message } from '../../services/chat.service';
import { UserService, User } from '../../services/user.service';
import { LucideAngularModule, Send, User as UserIcon, Search, MoreVertical, MessageSquare } from 'lucide-angular';
import { FormsModule } from '@angular/forms';
import { interval, Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  contacts: User[] = [];
  selectedContact: User | null = null;
  messages: Message[] = [];
  newMessage: string = '';
  searchQuery: string = '';
  loading = false;
  
  // Icons
  SendIcon = Send;
  UserIcon = UserIcon;
  SearchIcon = Search;
  MoreVerticalIcon = MoreVertical;
  MessageSquareIcon = MessageSquare;

  private pollSubscription?: Subscription;

  constructor(
    private chatService: ChatService,
    public userService: UserService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.loadContacts();
    
    // Check for query parameters to start a specific chat
    this.route.queryParams.subscribe(params => {
      const userId = params['userId'];
      const userName = params['userName'];
      
      if (userId && userName) {
        this.selectedContact = { userId: +userId, userName: userName } as User;
        this.loadMessages(this.selectedContact);
      }
    });

    // Poll for new messages every 3 seconds
    this.pollSubscription = interval(3000).subscribe(() => {
      if (this.selectedContact) {
        this.loadMessages(this.selectedContact);
      }
    });
  }

  ngOnDestroy() {
    this.pollSubscription?.unsubscribe();
  }

  loadContacts() {
    this.chatService.getContacts().subscribe({
      next: (data) => {
        this.contacts = data || [];
        // Auto-select first contact if none selected via query params
        if (!this.selectedContact && this.contacts.length > 0) {
          this.selectContact(this.contacts[0]);
        }
      }
    });
  }

  selectContact(contact: User) {
    this.selectedContact = contact;
    this.loadMessages(contact);
  }

  loadMessages(contact: User) {
    if (!contact.userId) return;
    this.chatService.getChatHistory(contact.userId).subscribe({
      next: (data) => {
        this.messages = data || [];
      }
    });
  }

  sendMessage() {
    if (!this.selectedContact?.userId || !this.newMessage.trim()) return;
    
    const msgText = this.newMessage.trim();
    this.newMessage = '';

    // Optimistic update: show message immediately in UI
    const tempMsg: Message = {
      msg: msgText,
      sender: this.userService.currentUser?.userId!,
      receiver: this.selectedContact.userId,
      createdAt: new Date().toISOString()
    };
    this.messages.push(tempMsg);

    this.chatService.sendMessage(this.selectedContact.userId, msgText).subscribe({
      next: () => {
        // Full sync will happen on next poll or we could force it here
      },
      error: () => {
        // Rollback or show error if send fails
        this.messages = this.messages.filter(m => m !== tempMsg);
        alert('Failed to send message');
      }
    });
  }

  getFilteredContacts() {
    if (!this.searchQuery) return this.contacts;
    return this.contacts.filter(c => 
      c.userName?.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }
}
