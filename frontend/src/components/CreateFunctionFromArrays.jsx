import React, { useState } from 'react';
import { functions } from '../services/api';
import Alert from './Alert';
import FunctionChart from './FunctionChart';
import './CreateFunction.css';

const CreateFunctionFromArrays = ({ onSuccess }) => {
  const [name, setName] = useState('');
  const [pointCount, setPointCount] = useState('');
  const [points, setPoints] = useState([]);
  const [functionType, setFunctionType] = useState('ARRAY');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [showTable, setShowTable] = useState(false);
  const [previewData, setPreviewData] = useState(null);

  const handlePointCountSubmit = () => {
    const count = parseInt(pointCount);

    if (isNaN(count) || count < 2) {
      setError('Количество точек должно быть не менее 2');
      return;
    }

    if (count > 1000) {
      setError('Слишком большое количество точек. Максимум 1000.');
      return;
    }

    setPoints(Array(count).fill().map(() => ({ x: '', y: '' })));
    setShowTable(true);
    setError('');
  };

  const handlePointChange = (index, field, value) => {
    const newPoints = [...points];
    newPoints[index][field] = value;
    setPoints(newPoints);
  };

  const handlePreview = () => {
    const xValues = points.map(p => parseFloat(p.x));
    const yValues = points.map(p => parseFloat(p.y));

    if (xValues.some(isNaN) || yValues.some(isNaN)) {
      setError('Все поля должны быть заполнены числами');
      return;
    }

    for (let i = 0; i < xValues.length - 1; i++) {
      if (xValues[i] >= xValues[i + 1]) {
        setError('Значения X должны быть строго возрастающими');
        return;
      }
    }

    setPreviewData({ name: name || 'Предпросмотр', xValues, yValues });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!name.trim()) {
      setError('Введите название функции');
      return;
    }

    const xValues = points.map(p => parseFloat(p.x));
    const yValues = points.map(p => parseFloat(p.y));

    if (xValues.some(isNaN) || yValues.some(isNaN)) {
      setError('Все поля должны быть заполнены числами');
      return;
    }

    for (let i = 0; i < xValues.length - 1; i++) {
      if (xValues[i] >= xValues[i + 1]) {
        setError('Значения X должны быть строго возрастающими');
        return;
      }
    }

    setLoading(true);
    try {
      await functions.createFromArrays(name, xValues, yValues, functionType);
      setSuccess('Функция успешно создана!');
      setName('');
      setPointCount('');
      setPoints([]);
      setShowTable(false);
      setPreviewData(null);
      if (onSuccess) onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при создании функции');
    }
    setLoading(false);
  };

  return (
    <div className="create-function">
      <h2>Создание функции из массивов</h2>

      <div className="form-group">
        <label>Название функции</label>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Моя функция"
        />
      </div>

      <div className="form-group">
        <label>Тип функции</label>
        <select value={functionType} onChange={(e) => setFunctionType(e.target.value)}>
          <option value="ARRAY">Массив</option>
          <option value="LINKED_LIST">Связный список</option>
        </select>
      </div>

      <div className="form-group">
        <label>Количество точек</label>
        <div className="input-with-button">
          <input
            type="number"
            value={pointCount}
            onChange={(e) => setPointCount(e.target.value)}
            placeholder="Введите количество точек"
            min="2"
          />
          <button type="button" onClick={handlePointCountSubmit} className="btn-secondary">
            {showTable ? 'Обновить' : 'Создать таблицу'}
          </button>
        </div>
      </div>

      {showTable && (
        <form onSubmit={handleSubmit}>
          <div className="points-table">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>X</th>
                  <th>Y</th>
                </tr>
              </thead>
              <tbody>
                {points.map((point, index) => (
                  <tr key={index}>
                    <td>{index + 1}</td>
                    <td>
                      <input
                        type="number"
                        step="any"
                        value={point.x}
                        onChange={(e) => handlePointChange(index, 'x', e.target.value)}
                        required
                      />
                    </td>
                    <td>
                      <input
                        type="number"
                        step="any"
                        value={point.y}
                        onChange={(e) => handlePointChange(index, 'y', e.target.value)}
                        required
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="button-group">
            <button type="button" onClick={handlePreview} className="btn-secondary">
              Предпросмотр графика
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Создание...' : 'Создать функцию'}
            </button>
          </div>
        </form>
      )}

      {previewData && <FunctionChart functionData={previewData} />}

      <Alert message={error} type="error" onClose={() => setError('')} />
      <Alert message={success} type="success" onClose={() => setSuccess('')} />
    </div>
  );
};

export default CreateFunctionFromArrays;
