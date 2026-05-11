import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { UserService, User } from '../../services/user.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: '../login/login.component.css'
})
export class RegisterComponent {
  user: User = { userName: '', userEmail: '', password: '' };
  error = '';
  loading = false;

  constructor(private userService: UserService, private router: Router) {}

  onSubmit() {
    if (!this.user.userName || !this.user.userEmail || !this.user.password) return;
    this.loading = true;
    this.error = '';

    this.userService.register(this.user).subscribe({
      next: () => {
        // Automatically login after register
        this.userService.login(this.user.userEmail, this.user.password!).subscribe({
          next: () => {
            this.userService.getProfile().subscribe({
              next: () => this.router.navigate(['/feed']),
              error: () => { this.router.navigate(['/feed']); }
            });
          },
          error: () => this.router.navigate(['/login'])
        });
      },
      error: (err) => {
        this.error = err.error || 'Registration failed. Try a different email.';
        this.loading = false;
      }
    });
  }
}
