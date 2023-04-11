import { IInstance, NewInstance } from './instance.model';

export const sampleWithRequiredData: IInstance = {
  id: 8386,
};

export const sampleWithPartialData: IInstance = {
  id: 44093,
  countryName: 'Ergonomic',
};

export const sampleWithFullData: IInstance = {
  id: 44677,
  countryName: 'Synergized Books',
};

export const sampleWithNewData: NewInstance = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
