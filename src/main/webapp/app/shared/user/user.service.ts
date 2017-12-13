import {Injectable} from '@angular/core';

import * as context_info from './context-info.json';

@Injectable()
export class UserService {
  constructor() {}

  getContextInfo(): Object {
    return context_info;
  }
}
