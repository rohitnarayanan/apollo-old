import {Routes} from '@angular/router';

import {ErrorComponent} from '../error/error.component';

export const errorRoutes: Routes = [
  {
    path: 'error',
    component: ErrorComponent,
    data: {
      authorities: [],
      statusCode: 500
    },
  },
  {
    path: 'accessdenied',
    component: ErrorComponent,
    data: {
      authorities: [],
      statusCode: 403
    },
  }
];
