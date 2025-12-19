import React, { useState, useEffect } from 'react';
import { functions } from '../services/api';
import Alert from './Alert';
import FunctionChart from './FunctionChart';
import './CreateFunction.css';

const CreateFunctionFromMath = ({ onSuccess }) => {
  const [name, setName] = useState('');
  const [mathFunctionType, setMathFunctionType] = useState('');
  const [mathTypes, setMathTypes] = useState([]);
  const [constantValue, setConstantValue] = useState('');
  const [xFrom, setXFrom] = useState('');
  const [xTo, setXTo] = useState('');
  const [count, setCount] = useState('');
  const [functionType, setFunctionType] = useState('ARRAY');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [previewData, setPreviewData] = useState(null);

  useEffect(() => {
    loadMathTypes();
  }, []);

  const loadMathTypes = async () => {
    try {
      const response = await functions.getMathTypes();
      const types = response.data;
      setMathTypes(types);
      // Устанавливаем первый тип по умолчанию
      if (types.length > 0) {
        setMathFunctionType(types[0].type);
      }
    } catch (err) {
      console.error('Failed to load math types', err);
    }
  };

  const handlePreview = async () => {
    const errors = validateInputs();
    if (errors.length > 0) {
      setError(errors.join('. '));
      return;
    }

    setLoading(true);
    try {
      const requestData = {
        name: 'Предпросмотр',
        mathFunctionType,
        xFrom: parseFloat(xFrom),
        xTo: parseFloat(xTo),
        count: parseInt(count),
        functionType,
        constantValue: constantValue ? parseFloat(constantValue) : null
      };

      const response = await functions.previewFromMath(requestData);
      setPreviewData(response.data);
      setError('');
    } catch (err) {
      console.error('Ошибка предпросмотра:', err.response?.data);
      const errorData = err.response?.data;
      if (errorData) {
        const errorMessages = [];
        Object.keys(errorData).forEach(key => {
          if (key !== 'message') {
            errorMessages.push(`${key}: ${errorData[key]}`);
          }
        });
        if (errorMessages.length > 0) {
          setError(errorData.message + '\n' + errorMessages.join('\n'));
        } else {
          setError(errorData.message || 'Ошибка при создании превью');
        }
      } else {
        setError('Ошибка при создании превью');
      }
    }
    setLoading(false);
  };

  const validateInputs = () => {
    const errors = [];

    if (!name.trim()) {
      errors.push('Введите название функции');
    }

    if (mathFunctionType === 'ConstantFunction' && !constantValue) {
      errors.push('Введите значение константы');
    }

    if (!xFrom || isNaN(parseFloat(xFrom))) {
      errors.push('Введите корректное начальное значение X');
    }

    if (!xTo || isNaN(parseFloat(xTo))) {
      errors.push('Введите корректное конечное значение X');
    }

    if (!count || isNaN(parseInt(count)) || parseInt(count) < 2) {
      errors.push('Количество точек должно быть не менее 2');
    }

    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const errors = validateInputs();
    if (errors.length > 0) {
      setError(errors.join('. '));
      return;
    }

    setLoading(true);
    try {
      const requestData = {
        name,
        mathFunctionType,
        xFrom: parseFloat(xFrom),
        xTo: parseFloat(xTo),
        count: parseInt(count),
        functionType,
        constantValue: constantValue ? parseFloat(constantValue) : null
      };

      await functions.createFromMath(requestData);
      setSuccess('Функция успешно создана!');
      setName('');
      setXFrom('');
      setXTo('');
      setCount('');
      setConstantValue('');
      setPreviewData(null);
      if (onSuccess) onSuccess();
    } catch (err) {
      console.error('Ошибка создания функции:', err.response?.data);
      const errorData = err.response?.data;
      if (errorData) {
        const errorMessages = [];
        Object.keys(errorData).forEach(key => {
          if (key !== 'message') {
            errorMessages.push(`${key}: ${errorData[key]}`);
          }
        });
        if (errorMessages.length > 0) {
          setError(errorData.message + '\n' + errorMessages.join('\n'));
        } else {
          setError(errorData.message || 'Ошибка при создании функции');
        }
      } else {
        setError('Ошибка при создании функции');
      }
    }
    setLoading(false);
  };

  return (
    <div className="create-function">
      <h2>Создание функции из математической функции</h2>

      <form onSubmit={handleSubmit}>
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
          <label>Математическая функция</label>
          <select
            value={mathFunctionType}
            onChange={(e) => setMathFunctionType(e.target.value)}
          >
            {mathTypes.map((type) => (
              <option key={type.type} value={type.type}>
                {type.name}
              </option>
            ))}
          </select>
        </div>

        {mathFunctionType === 'ConstantFunction' && (
          <div className="form-group">
            <label>Значение константы</label>
            <input
              type="number"
              step="any"
              value={constantValue}
              onChange={(e) => setConstantValue(e.target.value)}
              placeholder="Введите значение"
            />
          </div>
        )}

        <div className="form-group">
          <label>Тип реализации</label>
          <select value={functionType} onChange={(e) => setFunctionType(e.target.value)}>
            <option value="ARRAY">Массив</option>
            <option value="LINKED_LIST">Связный список</option>
          </select>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>X от</label>
            <input
              type="number"
              step="any"
              value={xFrom}
              onChange={(e) => setXFrom(e.target.value)}
              placeholder="0"
            />
          </div>

          <div className="form-group">
            <label>X до</label>
            <input
              type="number"
              step="any"
              value={xTo}
              onChange={(e) => setXTo(e.target.value)}
              placeholder="10"
            />
          </div>

          <div className="form-group">
            <label>Количество точек</label>
            <input
              type="number"
              value={count}
              onChange={(e) => setCount(e.target.value)}
              placeholder="10"
              min="2"
            />
          </div>
        </div>

        <div className="button-group">
          <button
            type="button"
            onClick={handlePreview}
            className="btn-secondary"
            disabled={loading}
          >
            Предпросмотр графика
          </button>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Создание...' : 'Создать функцию'}
          </button>
        </div>
      </form>

      {previewData && <FunctionChart functionData={previewData} />}

      <Alert message={error} type="error" onClose={() => setError('')} />
      <Alert message={success} type="success" onClose={() => setSuccess('')} />
    </div>
  );
};

export default CreateFunctionFromMath;
