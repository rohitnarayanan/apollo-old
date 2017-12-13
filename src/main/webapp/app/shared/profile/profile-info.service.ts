import {Injectable} from '@angular/core';

import {ProfileInfo} from './profile-info.model';

@Injectable()
export class ProfileInfoService {
  getProfileInfo(): ProfileInfo {
    return {
      activeProfiles: ['dev', 'swagger'],
      ribbonEnv: 'Development',
      inProduction: false,
      swaggerEnabled: true
    };
  }
}
