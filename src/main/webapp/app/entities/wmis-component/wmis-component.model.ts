import { IInstance } from 'app/entities/instance/instance.model';

export interface IWMISComponent {
  id: number;
  componentName?: string | null;
  description?: string | null;
  instance?: Pick<IInstance, 'id'> | null;
}

export type NewWMISComponent = Omit<IWMISComponent, 'id'> & { id: null };
