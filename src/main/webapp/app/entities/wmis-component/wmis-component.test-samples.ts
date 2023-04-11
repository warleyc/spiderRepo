import { IWMISComponent, NewWMISComponent } from './wmis-component.model';

export const sampleWithRequiredData: IWMISComponent = {
  id: 56327,
};

export const sampleWithPartialData: IWMISComponent = {
  id: 78135,
  description: 'system',
};

export const sampleWithFullData: IWMISComponent = {
  id: 67087,
  componentName: 'synthesize ivory',
  description: 'indexing',
};

export const sampleWithNewData: NewWMISComponent = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
