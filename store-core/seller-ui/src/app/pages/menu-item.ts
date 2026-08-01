import {NbMenuItem} from '@nebular/theme';
import {Roles} from './shared/models/roles';

export declare abstract class MenuItem extends NbMenuItem {
  key?: string;
  children?: MenuItem[];
  parent?: MenuItem;
  guards?: ((roles: Roles) => boolean)[];
}
