import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AddPersonComponent } from './components/add-person/add-person.component';
import { PeopleListComponent } from './components/people-list/people-list.component';
import { CityGroupComponent } from './components/city-group/city-group.component';
import { ByGroupComponent } from './components/by-group/by-group.component';
import { GuestGroupManagerComponent } from './components/guest-group-manager/guest-group-manager.component';
import { BudgetComponent } from './components/budget/budget.component';
import { IncomeComponent } from './components/income/income.component';
import { BudgetDashboardComponent } from './components/budget-dashboard/budget-dashboard.component';
import { AdminComponent } from './components/admin/admin.component';
import { BudgetReportComponent } from './components/budget-report/budget-report.component';
import { GuestCityReportComponent } from './components/guest-city-report/guest-city-report.component';
import { GiftReportComponent } from './components/gift-report/gift-report.component';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'add', component: AddPersonComponent },
  { path: 'list', component: PeopleListComponent },
  { path: 'city-group', component: CityGroupComponent },
  { path: 'by-group', component: ByGroupComponent },
  { path: 'guest-groups', component: GuestGroupManagerComponent },
  { path: 'budget', component: BudgetComponent },
  { path: 'income', component: IncomeComponent },
  { path: 'budget-dashboard', component: BudgetDashboardComponent },
  { path: 'report/budget', component: BudgetReportComponent },
  { path: 'report/guests-by-city', component: GuestCityReportComponent },
  { path: 'report/gifts', component: GiftReportComponent },
  { path: 'admin', component: AdminComponent }
];
