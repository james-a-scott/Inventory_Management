export type role = 'User' | 'SuperUser' | 'Admin';

export class User {
  email: string;
  name: string;
  role: role;

  constructor() {
    this.email = '';
    this.name = '';
    this.role = 'User';
  }
}
