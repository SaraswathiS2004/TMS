import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AddPersonComponent } from './components/add-person/add-person.component';
import { PeopleListComponent } from './components/people-list/people-list.component';
import { CityGroupComponent } from './components/city-group/city-group.component';
import { FunctionManagerComponent } from './components/function-manager/function-manager.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'add', component: AddPersonComponent },
  { path: 'list', component: PeopleListComponent },
  { path: 'city-group', component: CityGroupComponent },
  { path: 'functions', component: FunctionManagerComponent }
];
