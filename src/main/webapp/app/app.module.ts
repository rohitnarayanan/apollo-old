import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {Ng2Webstorage} from 'ngx-webstorage';

import {awfSharedModule} from './shared/shared.module';
import {HomeModule} from './home/home.module';

import {AppComponent} from './app.component';

import {
  RibbonComponent,
  HeaderComponent,
  ContentComponent,
  FooterComponent,
} from './layouts';


import {
  UserRouteAccessService,
  ProfileInfoService
} from './shared';


@NgModule({
  imports: [
    BrowserModule,
    Ng2Webstorage.forRoot({prefix: 'awf', separator: '-'}),
    awfSharedModule,
    HomeModule
  ],
  declarations: [
    AppComponent,
    RibbonComponent,
    HeaderComponent,
    ContentComponent,
    FooterComponent
  ],
  providers: [
    ProfileInfoService,
    UserRouteAccessService
  ],
  bootstrap: [AppComponent]
})

export class AppModule {}
