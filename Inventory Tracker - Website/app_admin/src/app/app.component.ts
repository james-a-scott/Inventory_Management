import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { ItemListingComponent } from './item-listing/item-listing.component';
import { NavbarComponent } from './navbar/navbar.component';

@Component({
  selector: 'app-root', // Root selector used in index.html
  standalone: true,     // Marks this as a standalone component (no need to declare in NgModule)
  imports: [            // External modules and components used within this component
    CommonModule,
    RouterOutlet,
    ItemListingComponent,
    NavbarComponent
  ],
  templateUrl: './app.component.html', // Path to the component's HTML template
  styleUrl: './app.component.css'      // Path to the component's CSS file
})
export class AppComponent {
  // Application title displayed in the navbar or page header
  title = 'Inventory - Admin!';
}
