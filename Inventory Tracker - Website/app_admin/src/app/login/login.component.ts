import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from "@angular/forms";
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';
import { User } from '../models/user';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, NavbarComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})

export class LoginComponent implements OnInit {
  // Variable to hold error messages
  public formError: string = '';

  // Model to store the user's credentials
  public credentials = {
    email: '',
    password: ''
  };

  constructor(
    private router: Router,  // To navigate to different pages
    private authenticationService: AuthenticationService // To handle authentication logic
  ) { }

  // Lifecycle hook called when the component is initialized
  ngOnInit() { }

  /**
   * Handles the form submission for login.
   * This function checks if all required fields are provided and calls the login process.
   * If any required fields are missing, an error message is displayed.
   * 
   * @throws Error - Catches and handles errors related to empty fields or authentication failures.
   */
  public onLoginSubmit(): void {
    this.formError = ''; // Reset error message before validation
    try {
      // Check if email and password fields are empty
      if (!this.credentials.email || !this.credentials.password) {
        // If any required field is empty, set the error message
        this.formError = 'All fields are required, please try again';
        this.router.navigateByUrl('#'); // Stay on login page
      } else {
        // If fields are valid, proceed with login attempt
        this.doLogin();
      }
    } catch (error) {
      // Handle unexpected errors and log them for debugging
      console.error('Error in onLoginSubmit:', error);
      this.formError = 'An error occurred while processing your login. Please try again.';  // User-friendly error message
    }
  }

  /**
   * Attempts to log in the user using the provided credentials.
   * This function interacts with the authentication service to authenticate the user
   * and navigate to the home page upon successful login.
   * 
   * @throws Error - Catches and handles any authentication or routing failures.
   */
  private doLogin(): void {
    try {
      // Create a new user object with the email from the form
      let newUser = {
        email: this.credentials.email,
      } as User;

      // Attempt to authenticate the user by passing the credentials to the authentication service
      this.authenticationService.login(newUser, this.credentials.password);

      // If authentication is successful, navigate to the home page
      if (this.authenticationService.isLoggedIn()) {
        this.router.navigate(['']);
      } else {
        // If the login fails, attempt to navigate after a delay to give user feedback
        setTimeout(() => {
          // If the login is successful after the delay, navigate to the home page
          if (this.authenticationService.isLoggedIn()) {
            this.router.navigate(['']);
          }
        }, 3000); // 3-second delay before retrying the login check
      }
    } catch (error) {
      // Log any errors that happen during the login attempt and provide a user-friendly message
      console.error('Error in doLogin:', error);
      this.formError = 'An error occurred while logging you in. Please try again.'; // User-friendly error message
    }
  }
}
