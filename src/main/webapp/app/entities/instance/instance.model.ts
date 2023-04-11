import { IApplication } from 'app/entities/application/application.model';

export interface IInstance {
  id: number;
  countryName?: string | null;
  application?: Pick<IApplication, 'id'> | null;
}

export type NewInstance = Omit<IInstance, 'id'> & { id: null };
