import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import FunctionChart2 from '../components/FunctionChart2';
import { useFactory } from '../context/FactoryContext';
import { functions } from '../services/api';
import './FunctionExplorer.css';

const FunctionExplorer = () => {
  const { factoryType } = useFactory();
  const [userFunctions, setUserFunctions] = useState([]);
  const [selectedFunctionId, setSelectedFunctionId] = useState('');
  const [selectedFunction, setSelectedFunction] = useState(null);
  const [xValue, setXValue] = useState('');
  const [computedY, setComputedY] = useState(null);
  const [error, setError] = useState('');
  const [editedYValues, setEditedYValues] = useState([]);
  const [isModified, setIsModified] = useState(false);
  const [insertX, setInsertX] = useState('');
  const [insertY, setInsertY] = useState('');
  const [showInsertForm, setShowInsertForm] = useState(false);
  const [coordinateSystem, setCoordinateSystem] = useState('cartesian');

  useEffect(() => {
    loadFunctions();
  }, []);

  const loadFunctions = async () => {
    try {
      const response = await functions.getAll();
      setUserFunctions(response.data);
    } catch (err) {
      setError('Ошибка загрузки функций');
      console.error(err);
    }
  };

  const handleFunctionSelect = async (e) => {
    const functionId = e.target.value;
    setSelectedFunctionId(functionId);
    setComputedY(null);
    setXValue('');
    setError('');
    setIsModified(false);

    if (functionId) {
      try {
        const response = await functions.getById(functionId);
        setSelectedFunction(response.data);
        setEditedYValues([...response.data.yValues]);
      } catch (err) {
        setError('Ошибка загрузки функции');
        console.error(err);
      }
    } else {
      setSelectedFunction(null);
    }
  };

  const handleApply = async () => {
    if (!selectedFunctionId || xValue === '') {
      setError('Выберите функцию и введите значение X');
      return;
    }

    try {
      const response = await functions.apply(parseInt(selectedFunctionId), parseFloat(xValue));
      setComputedY(response.data.result);
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка вычисления');
      setComputedY(null);
    }
  };

  const handleYValueChange = (index, value) => {
    const newYValues = [...editedYValues];
    newYValues[index] = parseFloat(value) || 0;
    setEditedYValues(newYValues);
    setIsModified(true);
  };

  const handleSaveChanges = async () => {
    try {
      await functions.update(selectedFunction.id, {
        name: selectedFunction.name,
        xValues: selectedFunction.xValues,
        yValues: editedYValues,
      });
      setError('');
      alert('Изменения сохранены!');
      setIsModified(false);
      // Перезагружаем функцию
      const response = await functions.getById(selectedFunction.id);
      setSelectedFunction(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка сохранения');
    }
  };

  const handleResetChanges = () => {
    if (selectedFunction) {
      setEditedYValues([...selectedFunction.yValues]);
      setIsModified(false);
    }
  };

  const handleInsertPoint = async () => {
    if (insertX === '' || insertY === '') {
      setError('Введите значения X и Y для вставки');
      return;
    }

    try {
      const response = await functions.insertPoint(
        selectedFunction.id,
        parseFloat(insertX),
        parseFloat(insertY)
      );
      setSelectedFunction(response.data);
      setEditedYValues([...response.data.yValues]);
      setInsertX('');
      setInsertY('');
      setShowInsertForm(false);
      setError('');
      alert('Точка успешно вставлена!');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка вставки точки');
    }
  };

  const handleRemovePoint = async (index) => {
    if (!window.confirm(`Удалить точку с индексом ${index}?`)) {
      return;
    }

    try {
      const response = await functions.removePoint(selectedFunction.id, index);
      setSelectedFunction(response.data);
      setEditedYValues([...response.data.yValues]);
      setError('');
      alert('Точка успешно удалена!');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка удаления точки');
    }
  };

  // Создаем данные для графика с отредактированными Y значениями
  const getChartData = () => {
    if (!selectedFunction) return null;
    return {
      ...selectedFunction,
      yValues: editedYValues,
    };
  };

  return (
    <>
      <Navbar />
      <div className="explorer-page">
        <div className="explorer-container">
          <h1>Изучение функции</h1>
          <p className="page-description">
            Выберите функцию для изучения, изменения значений и вычисления в произвольной точке.
            Используется фабрика: <strong>{factoryType === 'ARRAY' ? 'Массив' : 'Связный список'}</strong>
          </p>

          {error && <div className="error-message">{error}</div>}

          <div className="explorer-controls">
            <div className="control-group">
              <label htmlFor="function-select">Выберите функцию:</label>
              <select
                id="function-select"
                value={selectedFunctionId}
                onChange={handleFunctionSelect}
                className="function-select"
              >
                <option value="">-- Выберите функцию --</option>
                {userFunctions.map((func) => (
                  <option key={func.id} value={func.id}>
                    {func.name} ({func.count} точек)
                  </option>
                ))}
              </select>
            </div>
            {selectedFunction && (
              <div className="control-group">
                <label>Система координат</label>
                <select
                  value={coordinateSystem}
                  onChange={(e) => setCoordinateSystem(e.target.value)}
                  className="function-select"
                >
                  <option value="cartesian">Декартова</option>
                  <option value="polar">Полярная</option>
                  <option value="cylindrical">Цилиндрическая</option>
                  <option value="spherical">Сферическая</option>
                </select>
              </div>
            )}

            {selectedFunction && (
              <div className="apply-section">
                <h3>Вычислить значение apply(x)</h3>
                <div className="apply-controls">
                  <input
                    type="number"
                    step="any"
                    value={xValue}
                    onChange={(e) => setXValue(e.target.value)}
                    placeholder="Введите значение X"
                    className="x-input"
                  />
                  <button onClick={handleApply} className="apply-btn">
                    Вычислить
                  </button>
                </div>
                {computedY !== null && (
                  <div className="result-display">
                    <strong>Результат:</strong> f({xValue}) = {computedY.toFixed(6)}
                  </div>
                )}
              </div>
            )}
          </div>

          {selectedFunction && (
            <div className="function-display">
              <div className="function-header">
                <h2>{selectedFunction.name}</h2>
                <div className="header-buttons">
                  {isModified && (
                    <div className="modification-buttons">
                      <button onClick={handleSaveChanges} className="save-btn">
                        💾 Сохранить изменения
                      </button>
                      <button onClick={handleResetChanges} className="reset-btn">
                        ↺ Отменить
                      </button>
                    </div>
                  )}
                  <div className="action-buttons">
                    <button
                      onClick={() => setShowInsertForm(!showInsertForm)}
                      className="insert-btn"
                    >
                      ➕ Вставить точку
                    </button>
                  </div>
                </div>
              </div>

              {showInsertForm && (
                <div className="insert-form">
                  <h3>Вставить новую точку</h3>
                  <div className="insert-inputs">
                    <input
                      type="number"
                      step="any"
                      value={insertX}
                      onChange={(e) => setInsertX(e.target.value)}
                      placeholder="Значение X"
                      className="insert-input"
                    />
                    <input
                      type="number"
                      step="any"
                      value={insertY}
                      onChange={(e) => setInsertY(e.target.value)}
                      placeholder="Значение Y"
                      className="insert-input"
                    />
                    <button onClick={handleInsertPoint} className="confirm-insert-btn">
                      Вставить
                    </button>
                    <button
                      onClick={() => {
                        setShowInsertForm(false);
                        setInsertX('');
                        setInsertY('');
                      }}
                      className="cancel-insert-btn"
                    >
                      Отмена
                    </button>
                  </div>
                </div>
              )}

              <div className="function-content">
                <div className="function-table-wrapper">
                  <table className="function-table">
                    <thead>
                      <tr>
                        <th>Индекс</th>
                        <th>X</th>
                        <th>Y (редактируемый)</th>
                        <th>Действия</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedFunction.xValues.map((x, index) => (
                        <tr key={index}>
                          <td>{index}</td>
                          <td>{x.toFixed(4)}</td>
                          <td>
                            <input
                              type="number"
                              step="any"
                              value={editedYValues[index]}
                              onChange={(e) => handleYValueChange(index, e.target.value)}
                              className="y-input"
                            />
                          </td>
                          <td>
                            <button
                              onClick={() => handleRemovePoint(index)}
                              className="remove-point-btn"
                              title="Удалить точку"
                            >
                              🗑️
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="function-chart-wrapper">
                  <h3>График функции</h3>
                  <FunctionChart2 data={getChartData()} coordinateSystem={coordinateSystem} />
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default FunctionExplorer;
