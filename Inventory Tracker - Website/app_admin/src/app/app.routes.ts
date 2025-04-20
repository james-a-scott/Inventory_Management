import { Routes } from '@angular/router';

import { AddItemComponent } from './add-item/add-item.component';
import { ItemListingComponent } from './item-listing/item-listing.component';
import { EditItemComponent } from './edit-item/edit-item.component';
import { DeleteItemComponent } from './delete-item/delete-item.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';

// Define application routes using Angular's Routes type
export const routes: Routes = [
  // Route for adding a new inventory item
  { path: 'add-item', component: AddItemComponent },

  // Route for editing an existing inventory item based on item code
  // ":code" is a route parameter used to identify which item to edit
  { path: 'edit-item/:code', component: EditItemComponent },

  // Route for deleting inventory items
  { path: 'delete-item', component: DeleteItemComponent },

  // User login route
  { path: 'login', component: LoginComponent },

  // User registration route
  { path: 'register', component: RegisterComponent },

  // Default route - displays a list of items (home page)
  // `pathMatch: 'full'` ensures exact matching to the root URL
  { path: '', component: ItemListingComponent, pathMatch: 'full' }
];
