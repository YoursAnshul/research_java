import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthenticationService } from '../services/authentication/authentication.service';
import { UserRole } from '../models/presentation/enums';
import { IAuthenticatedUser } from '../interfaces/interfaces';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  constructor(
    private authService: AuthenticationService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    // List of routes that should be restricted from Interviewers
    const restrictedRoutes = ['users', 'forecasting', 'requests', 'configuration'];
    
    // If the current route is not in the restricted list, allow access
    if (!restrictedRoutes.includes(route.routeConfig?.path || '')) {
      return new Observable(observer => {
        observer.next(true);
        observer.complete();
      });
    }

    // First check if we're already authenticated
    if (this.authService.isAuthenticated.value === undefined) {
      // If not, trigger the auth check and wait for it
      console.log('RoleGuard: Triggering auth check');
      return new Observable<boolean>(observer => {
        this.authService.checkAuthenticatedUser().subscribe({
          next: (response) => {
            if ((response.Status || '').toUpperCase() !== 'SUCCESS') {
              console.log('RoleGuard: Auth check failed');
              observer.next(false);
              observer.complete();
              return;
            }

            const user = response.Subject as IAuthenticatedUser;
            if (user.role === UserRole.Interviewer) {
              console.log('RoleGuard: User is an Interviewer, blocking access');
              this.router.navigate(['/home']);
              observer.next(false);
            } else {
              console.log('RoleGuard: Access granted for role:', user.role);
              observer.next(true);
            }
            observer.complete();
          },
          error: (error) => {
            console.error('RoleGuard: Auth check error', error);
            observer.next(false);
            observer.complete();
          }
        });
      });
    }

    // If we're already authenticated, use the current user data
    return this.authService.authenticatedUser.pipe(
      map(user => {
        if (!user || Object.keys(user).length === 0) {
          console.log('RoleGuard: No user data available');
          return false;
        }

        if (user.role === UserRole.Interviewer) {
          console.log('RoleGuard: User is an Interviewer, blocking access');
          this.router.navigate(['/home']);
          return false;
        }

        console.log('RoleGuard: Access granted for role:', user.role);
        return true;
      })
    );
  }
}
