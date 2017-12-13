import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {
  ErrorComponent,
  ProfileInfoService,
  UserService
} from './';

import {
  awfRoutingModule
} from './routing.module';

@NgModule({
  imports: [
    awfRoutingModule
  ],
  declarations: [
  ],
  providers: [
    ProfileInfoService,
    UserService
  ],
  exports: [
    RouterModule
  ]
})
export class awfSharedModule {}
