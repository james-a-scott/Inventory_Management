import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';
import { User } from '../models/user';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, NavbarComponent],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  // Variable to store form error message
  public formError: string = '';

  // Object to hold user registration credentials
  public credentials = {
    name: '',
    email: '',
    password: '',
    role: ''
  };

  constructor(
    private router: Router, // Router to navigate between components
    private authenticationService: AuthenticationService // Authentication service to handle registration
  ) { }

  // On component initialization, no specific actions are required
  ngOnInit() { }

  /**
   * Handles the registration form submission.
   * Validates the form inputs and proceeds with registration if valid.
   */
  public onRegisterSubmit(): void {
    // Reset form error message
    this.formError = '';

    // Validate that all required fields are filled in
    if (!this.credentials.name || !this.credentials.email || !this.credentials.password || !this.credentials.role) {
      // If any field is missing, display error and stay on the current page
      this.formError = 'All fields are required, please try again';
      this.router.navigateByUrl('#'); // Stay on the register page
    } else {
      // Proceed with the registration process
      this.doRegister();
    }
  }

  /**
   * Registers a new user by calling the authentication service.
   * @returns void
   */
  private async doRegister(): Promise<void> {
    // Create a user object with the form data
    const newUser = {
      name: this.credentials.name,
      email: this.credentials.email,
      role: this.credentials.role
    } as User;

    try {
      // Attempt to register the user using the authentication service
      await this.authenticationService.register(newUser, this.credentials.password);

      // After successful registration, check if the user is logged in
      if (this.authenticationService.isLoggedIn()) {
        // Redirect to the home page if the user is successfully logged in
        this.router.navigate(['']);
      } else {
        // Set a timeout to check login status again if not logged in immediately
        const timer = setTimeout(() => {
          if (this.authenticationService.isLoggedIn()) {
            // Redirect to the home page after successful login
            this.router.navigate(['']);
          }
        }, 3000); // Timeout of 3 seconds
      }
    } catch (error) {
      // Handle any errors during registration, such as network issues or server errors
      console.error('Registration error:', error);

      // Display a user-friendly error message
      this.formError = 'An error occurred during registration. Please try again later.';
    }
  }

  /**
   * Navigates the user back to the previous page.
   */
  goBack() {
    window.history.back(); // Go back to the previous page
  }

}
