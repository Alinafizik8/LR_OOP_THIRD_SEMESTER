import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useFactory } from '../context/FactoryContext';
import { functions } from '../services/api';
import Navbar from '../components/Navbar';
import CreateFunctionFromArrays from '../components/CreateFunctionFromArrays';
import CreateFunctionFromMath from '../components/CreateFunctionFromMath';
import FunctionChart2 from '../components/FunctionChart2';
import Alert from '../components/Alert';
import './Dashboard.css';

const Dashboard = () => {
  const [activeTab, setActiveTab] = useState('list');
  const [userFunctions, setUserFunctions] = useState([]);
  const [selectedFunction, setSelectedFunction] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [editData, setEditData] = useState({ name: '', xValues: [], yValues: [] });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [coordinateSystem, setCoordinateSystem] = useState('cartesian');
  const { user, logout } = useAuth();
  const { factoryType } = useFactory();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const debouncedPreviewRef = useRef(null);
  const debouncedSubmitRef = useRef(null);

  useEffect(() => {
    loadFunctions();
  }, []);

  const loadFunctions = async () => {
    setLoading(true);
    try {
      const response = await functions.getAll();
      setUserFunctions(response.data);
    } catch (err) {
      setError('Ошибка при загрузке функций');
    }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Вы уверены, что хотите удалить эту функцию?')) {
      return;
    }

    try {
      await functions.delete(id);
      setSuccess('Функция успешно удалена');
      loadFunctions();
      if (selectedFunction?.id === id) {
        setSelectedFunction(null);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при удалении функции');
    }
  };

  const handleView = (func) => {
    setSelectedFunction(func);
    setEditMode(false);
    setActiveTab('view');
  };

  const handleEdit = (func) => {
    setSelectedFunction(func);
    setEditData({
      name: func.name,
      xValues: [...func.xValues],
      yValues: [...func.yValues],
    });
    setEditMode(true);
    setActiveTab('view');
  };

  const handleSaveEdit = async () => {
    // валидация перед отправкой
    const xValues = editData.xValues;
    if (xValues.some(isNaN)) {
      setError('Все значения X должны быть числами');
      return;
    }
    for (let i = 0; i < xValues.length - 1; i++) {
      if (xValues[i] >= xValues[i + 1]) {
        setError('Значения X должны быть строго возрастающими');
        return;
      }
    }

    try {
      await functions.update(selectedFunction.id, editData);
      setSuccess('Функция успешно обновлена');
      setEditMode(false);
      loadFunctions();
      setSelectedFunction({ ...selectedFunction, ...editData });
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при обновлении функции');
    }
  };

  const handlePointChange = (index, field, value) => {
    const newData = { ...editData };
    const numValue = value === '' ? NaN : parseFloat(value);

    if (field === 'x') {
      newData.xValues[index] = numValue;
    } else {
      newData.yValues[index] = numValue;
    }

    // Валидация: X-значения должны быть числами и строго возрастать
    const xValues = newData.xValues.map(x => x); // copy
    let hasError = false;

    if (xValues.some(isNaN)) {
      setError('Все значения X должны быть заполнены числами');
      hasError = true;
    } else {
      for (let i = 0; i < xValues.length - 1; i++) {
        if (xValues[i] >= xValues[i + 1]) {
          setError('Значения X должны быть строго возрастающими');
          hasError = true;
          break;
        }
      }
    }

    // Обновляем состояние в любом случае, но с/без ошибки
    setEditData(prev => {
        const xValues = [...prev.xValues];  // глубокая копия массива
        const yValues = [...prev.yValues];

        const numValue = value === '' ? NaN : parseFloat(value);

        if (field === 'x') {
          xValues[index] = numValue;
        } else {
          yValues[index] = numValue;
        }

        return { ...prev, xValues, yValues };
      });
    if (!hasError) {
      setError('');
    }
  };

  const handleAddPoint = () => {
    setEditData({
      ...editData,
      xValues: [...editData.xValues, 0],
      yValues: [...editData.yValues, 0],
    });
  };

  const handleRemovePoint = (index) => {
    if (editData.xValues.length <= 2) {
      setError('Функция должна содержать минимум 2 точки');
      return;
    }

    setEditData({
      ...editData,
      xValues: editData.xValues.filter((_, i) => i !== index),
      yValues: editData.yValues.filter((_, i) => i !== index),
    });
  };

  const handleDownload = async (func) => {
    try {
      const response = await functions.download(func.id);
      const base64Data = response.data.base64Data;

      // Преобразуем base64 в blob
      const binaryString = atob(base64Data);
      const bytes = new Uint8Array(binaryString.length);
      for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
      }
      const blob = new Blob([bytes], { type: 'application/octet-stream' });

      // Скачиваем файл
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${func.name}.bin`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      setSuccess('Функция успешно сохранена в файл');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при скачивании функции');
    }
  };

  const handleDownloadXml = async (func) => {
    try {
      const response = await functions.downloadXml(func.id);
      const xmlData = response.data.xmlData;

      const blob = new Blob([xmlData], { type: 'application/xml' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${func.name}.xml`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      setSuccess('Функция успешно сохранена в XML');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при скачивании XML');
    }
  };

  const handleDownloadJson = async (func) => {
    try {
      const response = await functions.downloadJson(func.id);
      const jsonData = response.data.jsonData;

      const blob = new Blob([jsonData], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${func.name}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      setSuccess('Функция успешно сохранена в JSON');
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при скачивании JSON');
    }
  };

  const handleUpload = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    try {
      const reader = new FileReader();
      reader.onload = async (e) => {
        const arrayBuffer = e.target.result;
        const bytes = new Uint8Array(arrayBuffer);
        let binaryString = '';
        for (let i = 0; i < bytes.length; i++) {
          binaryString += String.fromCharCode(bytes[i]);
        }
        const base64Data = btoa(binaryString);

        const functionName = file.name.replace(/\.(bin|dat)$/, '');

        const response = await functions.upload(functionName, base64Data, factoryType);
        setSuccess('Функция успешно загружена из файла');
        loadFunctions();
      };
      reader.readAsArrayBuffer(file);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при загрузке функции');
    }

    // Сбрасываем значение input для возможности повторной загрузки того же файла
    event.target.value = '';
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <>
      <Navbar />
      <div className="dashboard">

      <nav className="dashboard-tabs">
        <button
          className={activeTab === 'list' ? 'active' : ''}
          onClick={() => setActiveTab('list')}
        >
          Мои функции
        </button>
        <button
          className={activeTab === 'create-arrays' ? 'active' : ''}
          onClick={() => setActiveTab('create-arrays')}
        >
          Создать из массивов
        </button>
        <button
          className={activeTab === 'create-math' ? 'active' : ''}
          onClick={() => setActiveTab('create-math')}
        >
          Создать из функции
        </button>
        {selectedFunction && (
          <button
            className={activeTab === 'view' ? 'active' : ''}
            onClick={() => setActiveTab('view')}
          >
            {editMode ? 'Редактировать' : 'Просмотр'}
          </button>
        )}
      </nav>

      <main className="dashboard-content">
        {activeTab === 'list' && (
          <div className="function-list">
            <div className="list-header">
              <h2>Список функций</h2>
              <button onClick={handleUpload} className="btn-upload">
                📁 Загрузить из файла
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept=".bin,.dat"
                onChange={handleFileChange}
                style={{ display: 'none' }}
              />
            </div>
            {loading ? (
              <p>Загрузка...</p>
            ) : userFunctions.length === 0 ? (
              <p className="empty-message">У вас пока нет функций. Создайте свою первую функцию!</p>
            ) : (
              <div className="functions-grid">
                {userFunctions.map((func) => (
                  <div key={func.id} className="function-card">
                    <h3>{func.name}</h3>
                    <p>Тип: {func.functionType === 'ARRAY' ? 'Массив' : 'Связный список'}</p>
                    <p>Точек: {func.count}</p>
                    <div className="function-actions">
                      <button onClick={() => handleView(func)} className="btn-view">
                        Просмотр
                      </button>
                      <button onClick={() => handleEdit(func)} className="btn-edit">
                        Редактировать
                      </button>
                      <button onClick={() => handleDownload(func)} className="btn-download">
                        💾 BIN
                      </button>
                      <button onClick={() => handleDownloadXml(func)} className="btn-download-xml">
                        📄 XML
                      </button>
                      <button onClick={() => handleDownloadJson(func)} className="btn-download-json">
                        📋 JSON
                      </button>
                      <button onClick={() => handleDelete(func.id)} className="btn-delete">
                        Удалить
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'create-arrays' && (
          <CreateFunctionFromArrays
            onSuccess={() => {
              loadFunctions();
              setActiveTab('list');
            }}
          />
        )}

        {activeTab === 'create-math' && (
          <CreateFunctionFromMath
            onSuccess={() => {
              loadFunctions();
              setActiveTab('list');
            }}
          />
        )}

        {activeTab === 'view' && selectedFunction && (
          <div className="function-view">
            {editMode ? (
              <div className="edit-function">
                <h2>Редактирование функции</h2>
                <div className="form-group">
                  <label>Название</label>
                  <input
                    type="text"
                    value={editData.name}
                    onChange={(e) => setEditData({ ...editData, name: e.target.value })}
                  />
                </div>

                <div className="points-editor">
                  <h3>Точки функции</h3>
                  <table>
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>X</th>
                        <th>Y</th>
                        <th>Действия</th>
                      </tr>
                    </thead>
                    <tbody>
                      {editData.xValues.map((x, index) => (
                        <tr
                          key={index}
                          style={{
                            backgroundColor:
                              !isNaN(editData.xValues[index]) &&
                              (index === 0 || editData.xValues[index - 1] < editData.xValues[index]) &&
                              (index === editData.xValues.length - 1 || editData.xValues[index] < editData.xValues[index + 1])
                                ? 'inherit'
                                : '#442222',
                          }}
                        >
                          <td>{index + 1}</td>
                          <td>
                            <input
                              type="number"
                              step="any"
                              value={x}
                              onChange={(e) => handlePointChange(index, 'x', e.target.value)}
                            />
                          </td>
                          <td>
                            <input
                              type="number"
                              step="any"
                              value={editData.yValues[index]}
                              onChange={(e) => handlePointChange(index, 'y', e.target.value)}
                            />
                          </td>
                          <td>
                            <button
                              onClick={() => handleRemovePoint(index)}
                              className="btn-remove"
                              disabled={editData.xValues.length <= 2}
                            >
                              Удалить
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <button onClick={handleAddPoint} className="btn-add">
                    Добавить точку
                  </button>
                </div>

                <div className="button-group">
                  <button onClick={() => setEditMode(false)} className="btn-secondary">
                    Отмена
                  </button>
                  <button onClick={handleSaveEdit} className="btn-primary">
                    Сохранить
                  </button>
                </div>
                <div className="form-group">
                  <label>Система координат</label>
                  <select
                    value={coordinateSystem}
                    onChange={(e) => setCoordinateSystem(e.target.value)}
                    style={{ width: '100%', padding: '6px', background: '#2a2a2a', border: '1px solid #444', color: '#fff' }}
                  >
                    <option value="cartesian">Декартова</option>
                    <option value="polar">Полярная</option>
                    <option value="cylindrical">Цилиндрическая</option>
                    <option value="spherical">Сферическая</option>
                  </select>
                </div>
                <FunctionChart2 functionData={editData} coordinateSystem={coordinateSystem} />
              </div>
            ) : (
              <div className="view-function">
                <h2>{selectedFunction.name}</h2>
                <p>Тип: {selectedFunction.functionType === 'ARRAY' ? 'Массив' : 'Связный список'}</p>
                <p>Количество точек: {selectedFunction.count}</p>

                <div className="points-table">
                  <h3>Точки функции</h3>
                  <table>
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>X</th>
                        <th>Y</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedFunction.xValues.map((x, index) => (
                        <tr key={index}>
                          <td>{index + 1}</td>
                          <td>{x}</td>
                          <td>{selectedFunction.yValues[index]}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <div className="form-group">
                  <label>Система координат</label>
                  <select
                    value={coordinateSystem}
                    onChange={(e) => setCoordinateSystem(e.target.value)}
                    style={{ width: '100%', padding: '6px', background: '#2a2a2a', border: '1px solid #444', color: '#fff' }}
                  >
                    <option value="cartesian">Декартова</option>
                    <option value="polar">Полярная</option>
                    <option value="cylindrical">Цилиндрическая</option>
                    <option value="spherical">Сферическая</option>
                  </select>
                </div>

                <FunctionChart2 functionData={selectedFunction} coordinateSystem={coordinateSystem} />
              </div>
            )}
          </div>
        )}
      </main>

      <Alert message={error} type="error" onClose={() => setError('')} />
      <Alert message={success} type="success" onClose={() => setSuccess('')} />
      </div>
    </>
  );
};

export default Dashboard;
