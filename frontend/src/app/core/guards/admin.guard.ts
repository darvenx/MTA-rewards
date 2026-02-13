import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';
import { getRoleFromToken } from '../utils/auth-role.util';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(private tokenStorage: TokenStorageService, private router: Router) {}

  canActivate(): boolean | UrlTree {
    const role = getRoleFromToken(this.tokenStorage.getToken());
    if (role === 'ADMIN') {
      return true;
    }
    return this.router.createUrlTree(['/dashboard']);
  }
}
