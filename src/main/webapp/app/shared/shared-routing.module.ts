import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {errorRoutes} from './';


const LAYOUT_ROUTES = [
  ...errorRoutes
];

@NgModule({
  imports: [
    RouterModule.forRoot(LAYOUT_ROUTES, {useHash: true})
  ],
  exports: [
    RouterModule
  ],
  declarations: [
  ]
})

export class awfSharedRoutingModule {}
