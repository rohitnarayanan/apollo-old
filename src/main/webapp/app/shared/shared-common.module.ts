import {registerLocaleData} from '@angular/common';
import {NgModule, LOCALE_ID} from '@angular/core';
import {Title} from '@angular/platform-browser';
import locale from '@angular/common/locales/en';

import {awfSharedLibsModule} from './shared-libs.module';
import {ErrorComponent} from './';

@NgModule({
  imports: [
    awfSharedLibsModule
  ],
  declarations: [
    ErrorComponent
  ],
  providers: [
    Title,
    {
      provide: LOCALE_ID,
      useValue: 'en'
    },
  ],
  exports: [
  ]
})

export class awfSharedCommonModule {
  constructor() {
    registerLocaleData(locale);
  }
}
