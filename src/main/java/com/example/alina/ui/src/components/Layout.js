import React, { useState } from 'react';
import {
  Box, AppBar, Toolbar, IconButton, Typography, Drawer, List, ListItem,
  ListItemIcon, ListItemText, Divider, CssBaseline, Button
} from '@mui/material';
import {
  Menu as MenuIcon, Dashboard as DashboardIcon, Functions as FunctionsIcon,
  Add as AddIcon, Calculate as CalculateIcon, TrendingUp as TrendingUpIcon,
  Settings as SettingsIcon, Logout as LogoutIcon, DarkMode as DarkModeIcon,
  LightMode as LightModeIcon, AccountCircle as AccountCircleIcon
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { Link, useNavigate } from 'react-router-dom';

const drawerWidth = 240;

const Layout = ({ children }) => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user, signOut } = useAuth();
  const { mode, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const handleDrawerToggle = () => setMobileOpen(!mobileOpen);
  const handleLogout = () => { signOut(); navigate('/login'); };

  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6" noWrap>LR OOP</Typography>
      </Toolbar>
      <Divider />
      <List>
        <ListItem button component={Link} to="/">
          <ListItemIcon><DashboardIcon /></ListItemIcon>
          <ListItemText primary="Панель" />
        </ListItem>
        <ListItem button component={Link} to="/functions">
          <ListItemIcon><FunctionsIcon /></ListItemIcon>
          <ListItemText primary="Мои функции" />
        </ListItem>
        <ListItem button component={Link} to="/functions/new">
          <ListItemIcon><AddIcon /></ListItemIcon>
          <ListItemText primary="Создать" />
        </ListItem>
        <ListItem button component={Link} to="/operations">
          <ListItemIcon><CalculateIcon /></ListItemIcon>
          <ListItemText primary="Операции" />
        </ListItem>
        <ListItem button component={Link} to="/differentiation">
          <ListItemIcon><TrendingUpIcon /></ListItemIcon>
          <ListItemText primary="Дифференц." />
        </ListItem>
      </List>
      <Divider />
      <List>
        <ListItem button onClick={toggleTheme}>
          <ListItemIcon>{mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}</ListItemIcon>
          <ListItemText primary={mode === 'dark' ? 'Светлая тема' : 'Темная тема'} />
        </ListItem>
        <ListItem button component={Link} to="/settings">
          <ListItemIcon><SettingsIcon /></ListItemIcon>
          <ListItemText primary="Настройки" />
        </ListItem>
        <ListItem button onClick={handleLogout}>
          <ListItemIcon><LogoutIcon /></ListItemIcon>
          <ListItemText primary="Выход" />
        </ListItem>
      </List>
      <Box sx={{ p: 2 }}>
        <Typography variant="body2" color="text.secondary">
          {user?.username} • {user?.role || 'USER'}
        </Typography>
      </Box>
    </div>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar position="fixed" sx={{ width: { sm: `calc(100% - ${drawerWidth}px)` }, ml: { sm: `${drawerWidth}px` } }}>
        <Toolbar>
          <IconButton color="inherit" edge="start" onClick={handleDrawerToggle} sx={{ mr: 2, display: { sm: 'none' } }}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap sx={{ flexGrow: 1 }}>LR OOP — Табулированные функции</Typography>
          <IconButton color="inherit" onClick={toggleTheme}>
            {mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}
          </IconButton>
        </Toolbar>
      </AppBar>
      <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
        <Drawer variant="temporary" open={mobileOpen} onClose={handleDrawerToggle} sx={{ display: { xs: 'block', sm: 'none' }, '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth } }}>
          {drawer}
        </Drawer>
        <Drawer variant="permanent" sx={{ display: { xs: 'none', sm: 'block' }, '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth } }} open>
          {drawer}
        </Drawer>
      </Box>
      <Box component="main" sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - ${drawerWidth}px)` }, mt: 8 }}>
        {children}
      </Box>
    </Box>
  );
};

export default Layout;