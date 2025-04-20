import { Injectable, Inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { Item } from '../models/item';
import { User } from '../models/user';
import { AuthResponse } from '../models/authresponse';
import { BROWSER_STORAGE } from '../storage';

@Injectable({
  providedIn: 'root',
})
export class ItemDataService {
  constructor(
    private http: HttpClient, // HTTP client to make API requests
    @Inject(BROWSER_STORAGE) private storage: Storage // Local storage injection for token storage
  ) { }

  private itemUrl = 'http://localhost:3000/api/items'; // URL endpoint for item-related operations
  private baseUrl = 'http://localhost:3000/api'; // Base URL for authentication-related operations

  /**
   * Retrieves a list of all items.
   * This method makes a GET request to the item URL and returns the response as an observable.
   * @returns Observable<Item[]> An observable array of items.
   */
  getItems(): Observable<Item[]> {
    return this.http.get<Item[]>(this.itemUrl).pipe(
      catchError(this.handleError<Item[]>('getItems', [])) // Handles any errors that occur during the HTTP request
    );
  }

  /**
   * Adds a new item.
   * This method sends a POST request to the item URL with the item data to be added.
   * It includes a Bearer token in the headers for authentication.
   * @param formData The item data to be added.
   * @returns Observable<Item[]> An observable array of the updated list of items.
   */
  addItem(formData: Item): Observable<Item[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json', // Set the content type to JSON
      Authorization: `Bearer ${localStorage.getItem('inventory-token')}`, // Add Bearer token for authentication
    });
    return this.http.post<Item[]>(this.itemUrl, formData, { headers }).pipe(
      catchError(this.handleError<Item[]>('addItem', [])) // Handles any errors that occur during the POST request
    );
  }

  /**
   * Retrieves a single item by its unique code.
   * This method sends a GET request with the item code to fetch the specific item.
   * @param itemCode The unique item code.
   * @returns Observable<Item[]> An observable array containing the item data.
   */
  getItem(itemCode: string): Observable<Item[]> {
    return this.http.get<Item[]>(`${this.itemUrl}/${itemCode}`).pipe(
      catchError(this.handleError<Item[]>('getItem', [])) // Handles any errors that occur during the GET request
    );
  }

  /**
   * Updates an existing item.
   * This method sends a PUT request with the updated item data to the item URL, including the Bearer token in the headers.
   * @param formData The item data to be updated.
   * @returns Observable<Item[]> An observable array of the updated list of items.
   */
  updateItem(formData: Item): Observable<Item[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json', // Set the content type to JSON
      Authorization: `Bearer ${localStorage.getItem('inventory-token')}`, // Add Bearer token for authentication
    });
    return this.http.put<Item[]>(`${this.itemUrl}/${formData.code}`, formData, { headers }).pipe(
      catchError(this.handleError<Item[]>('updateItem', [])) // Handles any errors that occur during the PUT request
    );
  }

  /**
   * Deletes an item by its code.
   * This method sends a DELETE request with the item code to delete the specified item.
   * @param itemCode The unique item code to be deleted.
   * @returns Observable<Item[]> An observable array of the remaining items after deletion.
   */
  deleteItem(itemCode: string): Observable<Item[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json', // Set the content type to JSON
      Authorization: `Bearer ${localStorage.getItem('inventory-token')}`, // Add Bearer token for authentication
    });
    return this.http.delete<Item[]>(`${this.itemUrl}/${itemCode}`, { headers }).pipe(
      catchError(this.handleError<Item[]>('deleteItem', [])) // Handles any errors that occur during the DELETE request
    );
  }

  /**
   * Handles user login by sending credentials to the authentication API.
   * This method sends a POST request to the login endpoint with the user's login data.
   * @param user The user data for login.
   * @param passwd The user password.
   * @returns Observable<AuthResponse> An observable authentication response.
   */
  login(user: User, passwd: string): Observable<AuthResponse> {
    return this.handleAuthAPICall('login', user, passwd); // Handles login request
  }

  /**
   * Registers a new user by sending their data to the authentication API.
   * This method sends a POST request to the register endpoint with the user's registration data.
   * @param user The user data for registration.
   * @param passwd The user password.
   * @returns Observable<AuthResponse> An observable authentication response.
   */
  register(user: User, passwd: string): Observable<AuthResponse> {
    return this.handleAuthAPICall('register', user, passwd); // Handles registration request
  }

  /**
   * Handles the API call for both login and registration.
   * This method sends a POST request to the login or register endpoint with the user's data.
   * @param endpoint The endpoint for either 'login' or 'register'.
   * @param user The user data.
   * @param passwd The user password.
   * @returns Observable<AuthResponse> An observable authentication response.
   */
  private handleAuthAPICall(
    endpoint: string,
    user: User,
    passwd: string
  ): Observable<AuthResponse> {
    const formData = {
      name: user.name,
      email: user.email,
      role: user.role,
      password: passwd,
    };

    return this.http.post<AuthResponse>(`${this.baseUrl}/${endpoint}`, formData).pipe(
      catchError(this.handleError<AuthResponse>(`handleAuthAPICall ${endpoint}`)) // Handles any errors that occur during the POST request
    );
  }

  /**
   * Handles errors during HTTP calls.
   * This method logs the error to the console and returns a default result to keep the app running.
   * @param operation The name of the operation being executed.
   * @param result The default result to return if the API call fails.
   * @returns A function that handles the error and returns an observable of the default result.
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      // Log the error to the console
      console.error(`${operation} failed: ${error.message}`);
      // Return an empty result to keep the app running smoothly
      return of(result as T);
    };
  }
}
