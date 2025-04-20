import { Injectable, Provider } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { HttpInterceptor, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationService } from '../services/authentication.service';

/**
 * JwtInterceptor - Intercepts HTTP requests to add an Authorization token
 * to the headers if the user is logged in and the request is not for 
 * authentication endpoints (login/register).
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(
    private authenticationService: AuthenticationService
  ) { }

  /**
   * Intercept method for handling HTTP requests.
   * Adds the JWT token to the Authorization header of outgoing requests.
   * 
   * @param request - The HTTP request object.
   * @param next - The HTTP handler to forward the request.
   * @returns An observable of the HTTP event.
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let isAuthAPI: boolean;

    // Check if the request is for authentication API (login or register)
    if (request.url.startsWith('login') || request.url.startsWith('register')) {
      isAuthAPI = true; // Mark as authentication API, no token needed
    } else {
      isAuthAPI = false; // Mark as non-authentication API, token required
    }

    try {
      // Proceed to add the token to the request headers if the user is logged in
      if (this.authenticationService.isLoggedIn() && !isAuthAPI) {

        // Retrieve the JWT token from AuthenticationService
        let token = this.authenticationService.getToken();

        if (!token) {
          // Log an error if the token is not found or expired
          console.error('No token found for the user.');
        }

        // Clone the original request and add the Authorization header
        const authReq = request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}` // Attach token in the 'Authorization' header
          }
        });

        // Pass the cloned request along the chain
        return next.handle(authReq);
      }

      // If the user is not logged in or it's an authentication API, continue without modifying request
      return next.handle(request);

    } catch (error) {
      // Catch any errors during token retrieval, request cloning, or interception
      console.error('Error during request interception:', error);
      // Rethrow the error to ensure it propagates properly
      throw error;
    }
  }
}

/**
 * Exported provider to register JwtInterceptor in the app module
 * as part of the HTTP_INTERCEPTORS array.
 */
export const authInterceptProvider: Provider = {
  provide: HTTP_INTERCEPTORS,
  useClass: JwtInterceptor,
  multi: true // Ensure the interceptor is added to the chain of HTTP interceptors
};
