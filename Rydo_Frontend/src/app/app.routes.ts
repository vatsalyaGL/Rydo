import { Routes } from '@angular/router';
import { authGuard, adminGuard, userGuard } from './guards/auth.guard';
import { FormsComponent } from './pages/forms/forms.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    loadComponent: () => import('./pages/auth/auth.component').then(m => m.AuthComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'profile',
    loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [authGuard]
  },
  {
    path: 'driver/apply',
    loadComponent: () => import('./pages/driver/driver-apply.component').then(m => m.DriverApplyComponent),
    canActivate: [userGuard]
  },
  {
    path: 'driver/profile',
    loadComponent: () => import('./pages/driver/driver-profile.component').then(m => m.DriverProfileComponent),
    canActivate: [authGuard]
  },
  {
    path: 'driver/rides',
    loadComponent: () => import('./pages/driver/driver-rides.component').then(m => m.DriverRidesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'admin/drivers',
    loadComponent: () => import('./pages/admin/admin-drivers.component').then(m => m.AdminDriversComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'admin/users',
    loadComponent: () => import('./pages/admin/admin-users.component').then(m => m.AdminUsersComponent),
    canActivate: [authGuard, adminGuard]
  },
  {path: 'payment/:tripId',
  loadComponent: () =>
    import('./pages/payments/payments.component')
      .then(m => m.PaymentsComponent),
  canActivate: [authGuard]
  },
  { path: 'form', component: FormsComponent },
  { path: '**', redirectTo: 'dashboard' }
];
