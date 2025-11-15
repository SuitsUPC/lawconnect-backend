# 💳 Cómo Ver tus Créditos de GCP

## Tu Información de Billing

- **Cuenta de Billing:** 01EE32-83425B-1246EC
- **Nombre:** Mi cuenta de facturación
- **Estado:** Activa (Open: True)
- **Proyecto:** xantinamobileapp

## 🌐 Ver Créditos y Costos

### Opción 1: Consola Web (Recomendado)

Abre esta URL en tu navegador:
```
https://console.cloud.google.com/billing/01EE32-83425B-1246EC
```

En la consola podrás ver:
- ✅ **Créditos disponibles** (si tienes trial o promociones)
- ✅ **Costos del mes actual**
- ✅ **Historial de facturación**
- ✅ **Límites y alertas configuradas**
- ✅ **Proyección de costos**

### Opción 2: Desde la Consola Principal

1. Ve a: https://console.cloud.google.com/billing
2. Selecciona tu cuenta de billing
3. En el menú lateral verás:
   - **Overview**: Resumen de costos y créditos
   - **Costs**: Costos detallados
   - **Budgets & alerts**: Presupuestos y alertas
   - **Credits**: Créditos disponibles

## 💰 Créditos Gratuitos de GCP

Si estás en el período de prueba (trial):
- **$300 USD gratis** por 90 días
- Se muestran en la sección "Credits" de la consola
- Se aplican automáticamente a tus costos

## 📊 Comandos Útiles

```bash
# Ver cuentas de billing
gcloud billing accounts list

# Ver información de una cuenta
gcloud billing accounts describe 01EE32-83425B-1246EC

# Ver proyectos vinculados
gcloud billing projects list --billing-account=01EE32-83425B-1246EC

# Ver costos (requiere BigQuery)
# gcloud billing budgets list --billing-account=01EE32-83425B-1246EC
```

## ⚠️ Importante

- Los créditos gratuitos se aplican automáticamente
- Una vez agotados, se cobrará a tu método de pago
- Configura alertas para evitar sorpresas
- Revisa regularmente los costos

## 🔔 Configurar Alertas

Para recibir alertas cuando los costos suban:

1. Ve a: https://console.cloud.google.com/billing/budgets
2. Crea un nuevo presupuesto
3. Configura alertas (ej: 50%, 90%, 100% del presupuesto)

O desde la línea de comandos:
```bash
gcloud billing budgets create \
  --billing-account=01EE32-83425B-1246EC \
  --display-name="LawConnect Budget" \
  --budget-amount=30USD \
  --threshold-rule=percent=50 \
  --threshold-rule=percent=90 \
  --threshold-rule=percent=100
```

