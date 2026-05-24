import { inject, Injectable } from '@angular/core';
import { ApiService } from './api.service';
import type { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(ApiService);

  all() {
    return this.api.client.get<User[]>(`${this.api.baseUrl}/users`);
  }

  /** TEAM_LEADER only — members of the team they lead. */
  teamMembers() {
    return this.api.client.get<User[]>(`${this.api.baseUrl}/users/team-members`);
  }

  me() {
    return this.api.client.get<User>(`${this.api.baseUrl}/users/me`);
  }

  create(user: User) {
    return this.api.client.post<User>(`${this.api.baseUrl}/users`, user);
  }

  update(id: number, user: User) {
    return this.api.client.put<User>(`${this.api.baseUrl}/users/${id}`, user);
  }

  delete(id: number) {
    return this.api.client.delete(`${this.api.baseUrl}/users/${id}`);
  }
}

