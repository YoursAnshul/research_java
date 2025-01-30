import { Injectable } from '@angular/core';
import { CanDeactivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';

export interface CanComponentDeactivate {
  canDeactivate: (nextUrl: string | null) => boolean | Observable<boolean> | Promise<boolean>;
}

@Injectable({
  providedIn: 'root',
})
export class UnsavedChangesGuard implements CanDeactivate<CanComponentDeactivate> {
  constructor() {}

  canDeactivate(
    component: CanComponentDeactivate,
    currentRoute: ActivatedRouteSnapshot,
    currentState: RouterStateSnapshot,
    nextState?: RouterStateSnapshot // The target route (can be null for root navigation)
  ): boolean | Observable<boolean> | Promise<boolean> {
    const currentUrl = currentState.url; // The URL the user is currently on
    const nextUrl = nextState?.url || null; // The URL the user is navigating to (if available)
    // Pass both URLs to the component's `canDeactivate` method
    return component.canDeactivate
      ? component.canDeactivate(nextUrl)
      : true;
  }
}
