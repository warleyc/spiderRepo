export interface IApplication {
  id: number;
  applicationName?: string | null;
}

export type NewApplication = Omit<IApplication, 'id'> & { id: null };
