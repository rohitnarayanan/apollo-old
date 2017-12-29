import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {RouterModule} from '@angular/router';

import {awfSharedModule} from '../shared/shared.module';

import {homeRoutes, HomeComponent} from './';


@NgModule({
  imports: [
    awfSharedModule,
    RouterModule.forChild(homeRoutes)
  ],
  declarations: [
    HomeComponent,
  ],
  entryComponents: [
  ],
  providers: [
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})

export class HomeModule {}
