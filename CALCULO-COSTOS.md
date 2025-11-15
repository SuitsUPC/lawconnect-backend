# 💰 Cálculo de Costos Mensuales - VM GCP

## Configuración Actual

- **Tipo de VM:** e2-medium
- **Especificaciones:** 2 vCPU, 4 GB RAM
- **Disco:** 50 GB
- **Región:** southamerica-east1 (São Paulo, Brasil)
- **Uso:** 24/7 (730 horas/mes)

## Costos Estimados (USD/mes)

### 1. Instancia e2-medium
- **Precio por hora:** ~$0.033 USD
- **Por mes (730 horas):** ~$24.09 USD
- **Con descuento uso sostenido (20%):** ~$19.27 USD

### 2. Disco Persistente (50 GB)
- **Precio:** ~$0.04 USD/GB/mes
- **Total:** ~$2.00 USD/mes

### 3. Tráfico de Red (estimado)
- **Primeros 1 GB:** Gratis
- **Siguientes 9 GB:** ~$0.12 USD/GB = ~$1.08 USD
- **Siguientes 20 GB:** ~$0.12 USD/GB = ~$2.40 USD
- **Total estimado (30 GB):** ~$3.60 USD/mes

### 4. Otros costos
- **IP estática:** Gratis (si está en uso)
- **Snapshots:** Depende del uso

## 💵 COSTO TOTAL ESTIMADO

**Sin descuentos:** ~$30 USD/mes  
**Con descuento uso sostenido:** ~$25 USD/mes

## 💡 Opciones para Reducir Costos

### Opción 1: Cambiar a e2-micro (Desarrollo)
- **Especificaciones:** 2 vCPU compartidas, 1 GB RAM
- **Costo:** ~$6-8 USD/mes
- **Ideal para:** Desarrollo y pruebas

### Opción 2: Committed Use Discounts
- **Descuento:** Hasta 57% por compromiso de 1-3 años
- **Recomendado si:** Planeas usar la VM por más de 1 año

### Opción 3: Preemptible VMs
- **Descuento:** ~70% más barato
- **Desventaja:** Puede ser interrumpida (no recomendado para producción)

### Opción 4: Apagar cuando no se use
- **Ahorro:** Solo pagas cuando está encendida
- **Ideal para:** Desarrollo intermitente

## 📊 Monitoreo de Costos

1. **Google Cloud Console:**
   - https://console.cloud.google.com/billing

2. **Configurar alertas:**
   ```bash
   # Crear presupuesto con alertas
   gcloud billing budgets create \
     --billing-account=TU_BILLING_ACCOUNT \
     --display-name="LawConnect VM Budget" \
     --budget-amount=30USD \
     --threshold-rule=percent=50 \
     --threshold-rule=percent=90 \
     --threshold-rule=percent=100
   ```

3. **Ver costos actuales:**
   ```bash
   gcloud billing accounts list
   gcloud billing projects describe PROJECT_ID
   ```

## ⚠️ Importante

- Los precios pueden variar según la región y cambios de Google
- El tráfico de red puede aumentar significativamente con mucho uso
- Considera configurar alertas de billing para evitar sorpresas
- Revisa regularmente el uso en la consola de GCP

## 🔗 Enlaces Útiles

- **Calculadora de precios:** https://cloud.google.com/products/calculator
- **Precios oficiales:** https://cloud.google.com/compute/pricing
- **Descuentos:** https://cloud.google.com/compute/docs/sustained-use-discounts

