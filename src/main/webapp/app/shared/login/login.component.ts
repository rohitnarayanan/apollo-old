import {Component, AfterViewInit, Renderer, ElementRef} from '@angular/core';
import {Router} from '@angular/router';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

import {LoginService} from './login.service';
import {StateStorageService} from '../auth/state-storage.service';

@Component({
  selector: 'awf-login',
  templateUrl: './login.component.html'
})
export class LoginComponent implements AfterViewInit {
  authStatus: string;
  username: string;
  password: string;
  rememberMe: boolean;
  credentials: any;

  constructor(
    private loginService: LoginService,
    private stateStorageService: StateStorageService,
    private elementRef: ElementRef,
    private renderer: Renderer,
    private router: Router,
    public activeModal: NgbActiveModal
  ) {
    this.credentials = {};
  }

  ngAfterViewInit() {
    console.log("ngAfterViewInit");
    this.renderer.invokeElementMethod(this.elementRef.nativeElement.querySelector('#_username'), 'focus', []);
  }

  cancel() {
    this.credentials = {
      username: null,
      password: null,
      rememberMe: true
    };
    this.authStatus = null;
    this.activeModal.dismiss('cancel');
  }

  login() {
    this.loginService.login({
      username: this.username,
      password: this.password,
      rememberMe: this.rememberMe
    }).then(() => {
      this.authStatus = null;
      this.activeModal.dismiss('login success');
      if (this.router.url === '/register' || (/^\/activate\//.test(this.router.url)) ||
        (/^\/reset\//.test(this.router.url))) {
        this.router.navigate(['']);
      }

      /* 
       * previousState was set in the authExpiredInterceptor before being redirected to login modal. 
       * since login is successful, go to stored previousState and clear previousState 
       */
      const redirect = this.stateStorageService.getUrl();
      if (redirect) {
        this.stateStorageService.storeUrl(null);
        this.router.navigate([redirect]);
      }
    }).catch(() => {
      this.authStatus = 'error';
    });
  }

  register() {
    this.activeModal.dismiss('to state register');
    this.router.navigate(['/register']);
  }

  requestResetPassword() {
    this.activeModal.dismiss('to state requestReset');
    this.router.navigate(['/reset', 'request']);
  }
}
