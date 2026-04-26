import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AddPersonComponent } from './components/add-person/add-person.component';
import { PeopleListComponent } from './components/people-list/people-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'add', component: AddPersonComponent },
  { path: 'list', component: PeopleListComponent }
];
