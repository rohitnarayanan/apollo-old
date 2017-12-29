import {Injectable} from '@angular/core';

import {Principal} from '../auth/principal.service';
import {AuthService} from '../auth/auth.service';

@Injectable()
export class LoginService {

  constructor(
    private principal: Principal,
    private authService: AuthService
  ) {}

  login(credentials, callback?) {
    const cb = callback || function() {};

    return new Promise((resolve, reject) => {
      this.authService.login(credentials).subscribe((data) => {
        this.principal.identity(true).then((account) => {
          resolve(data);
        });
        return cb();
      }, (err) => {
        this.logout();
        reject(err);
        return cb(err);
      });
    });
  }

  loginWithToken(jwt, rememberMe) {
    return this.authService.loginWithToken(jwt, rememberMe);
  }

  logout() {
    this.authService.logout().subscribe();
    this.principal.authenticate(null);
  }
}
