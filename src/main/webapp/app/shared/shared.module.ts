import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {DatePipe} from '@angular/common';

import {awfSharedLibsModule} from './shared-libs.module';
import {awfSharedCommonModule} from './shared-common.module';
import {awfSharedRoutingModule} from './shared-routing.module';

import {
  AccountService,
  AuthService,
  CSRFService,
  IsAllowedDirective,
  Principal,
  StateStorageService,
  LoginService,
  LoginModalService,
  LoginComponent,
  ProfileInfoService,
  SocialComponent,
  SocialService,
  UserService
} from './';


@NgModule({
  imports: [
    awfSharedLibsModule,
    awfSharedCommonModule,
    awfSharedRoutingModule
  ],
  declarations: [
    IsAllowedDirective,
    LoginComponent,
    SocialComponent
  ],
  providers: [
    AccountService,
    AuthService,
    CSRFService,
    Principal,
    StateStorageService,
    LoginModalService,
    LoginService,
    ProfileInfoService,
    SocialService,
    UserService,
    DatePipe
  ],
  entryComponents: [LoginComponent],
  exports: [
    awfSharedLibsModule,
    awfSharedCommonModule,
    awfSharedRoutingModule,
    IsAllowedDirective,
    LoginComponent,
    SocialComponent,
    DatePipe
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})

export class awfSharedModule {}
