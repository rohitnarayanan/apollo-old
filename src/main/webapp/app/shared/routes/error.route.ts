import {Routes} from '@angular/router';

import {ErrorComponent} from '../error/error.component';

export const errorRoute: Routes = [
  {
    path: 'error',
    component: ErrorComponent,
    data: {
      authorities: []
    },
  },
  {
    path: 'accessdenied',
    component: ErrorComponent,
    data: {
      authorities: [],
      error403: true
    },
  }
];
