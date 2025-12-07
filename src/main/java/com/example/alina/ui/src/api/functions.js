import api from './api';

const getOwnerId = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (!user?.id) throw new Error('User ID not found');
  return user.id;
};

export const getAllFunctions = async () => {
  const ownerId = getOwnerId();
  const res = await api.get('/tabulated-functions', {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const getFunctionById = async (id) => {
  const ownerId = getOwnerId();
  const res = await api.get(`/tabulated-functions/${id}`, {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const createFunctionFromPoints = async ({ name, xValues, yValues }) => {
  const ownerId = getOwnerId();
  const dto = {
    name,
    xValues,
    yValues,
    ownerId
  };
  const res = await api.post('/tabulated-functions/from-points', dto, {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const createFunctionFromMath = async ({ name, mathFunctionType, xFrom, xTo, count }) => {
  const ownerId = getOwnerId();
  const dto = {
    name,
    mathFunctionType,
    xFrom,
    xTo,
    count,
    ownerId
  };
  const res = await api.post('/tabulated-functions/from-math', dto, {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const updateFunctionName = async (id, name) => {
  const ownerId = getOwnerId();
  await api.patch(`/tabulated-functions/${id}/name`, { name }, {
    headers: { 'X-User-Id': ownerId }
  });
};

export const updateFunctionDataAndName = async (id, { xValues, yValues, name }) => {
  const ownerId = getOwnerId();
  // Сериализуем в JSON: { x: [...], y: [...] }
  const data = new TextEncoder().encode(JSON.stringify({ x: xValues, y }));
  await api.patch(`/tabulated-functions/${id}/data-and-name`, { name, data }, {
    headers: { 'X-User-Id': ownerId }
  });
};

export const deleteFunction = async (id) => {
  const ownerId = getOwnerId();
  await api.delete(`/tabulated-functions/${id}`, {
    headers: { 'X-User-Id': ownerId }
  });
};

export const differentiateFunction = async ({ functionId, resultName }) => {
  const ownerId = getOwnerId();
  const res = await api.post('/tabulated-functions/differentiate', {
    functionId,
    resultName,
    ownerId
  }, {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};