import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';
import {HttpClientModule, HttpHeaders} from '@angular/common/http';

import {AppComponent} from './app.component';

import {
  RibbonComponent,
  HeaderComponent,
  ContentComponent,
  FooterComponent,
} from './layouts';

import {
  awfSharedModule
} from './shared/shared.module';

import {HomeComponent} from './home/home.component';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    awfSharedModule
  ],
  declarations: [
    AppComponent,
    RibbonComponent,
    HeaderComponent,
    ContentComponent,
    FooterComponent,
    HomeComponent
  ],
  bootstrap: [AppComponent]
})

export class AppModule {}
