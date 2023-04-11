import { IApplication, NewApplication } from './application.model';

export const sampleWithRequiredData: IApplication = {
  id: 99506,
};

export const sampleWithPartialData: IApplication = {
  id: 49138,
};

export const sampleWithFullData: IApplication = {
  id: 6690,
  applicationName: 'secured',
};

export const sampleWithNewData: NewApplication = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
