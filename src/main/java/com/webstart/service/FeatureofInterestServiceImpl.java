package com.webstart.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstart.DTO.*;
import com.webstart.Enums.FeatureTypeEnum;
import com.webstart.Enums.StatusTimeConverterEnum;
import com.webstart.Helpers.HelperCls;
import com.webstart.model.*;
import com.webstart.repository.FeatureofinterestJpaRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Service("featureofInterestService")
@Transactional
public class FeatureofInterestServiceImpl implements FeatureofInterestService {

    @Autowired
    FeatureofinterestJpaRepository featureofinterestJpaRepository;

    public boolean addCrop(Crop crop) {
        Featureofinterest featureofinterest = new Featureofinterest();

        crop.getCropname();
        crop.getCropdescription();
        crop.getStationname();
        crop.getStationdescription();
        crop.getDevices();

        featureofinterest.setHibernatediscriminator("N");
        featureofinterest.setFeatureofinteresttypeid(1);
        featureofinterest.setIdentifier(crop.getCropname());
        featureofinterest.setCodespaceid(Long.parseLong(null));
        featureofinterest.setName(crop.getCropdescription());
        featureofinterest.setDescriptionxml(null);
        featureofinterest.setUrl(null);
        featureofinterest.setGeom(null);

        //featureofinterest.setGeom(null);

        if ((featureofinterestJpaRepository.save(featureofinterest)) != null) {
            return true;
        }

        return false;
    }

    public JSONObject findCropInfo(int id) {
        List<Featureofinterest> featureofinterestList = featureofinterestJpaRepository.getFeaturesByUsrerId(id);
        List<Integer> featureids = new ArrayList<Integer>();

        JSONObject obj1 = new JSONObject();
        JSONArray list = new JSONArray();
        JSONObject obj = new JSONObject();

        for (int k = 0; k < featureofinterestList.size(); k++) {
            if (featureofinterestList.get(k).getFeatureofinteresttypeid() == FeatureTypeEnum.END_DEVICE.getValue()) {
                featureids.add(featureofinterestList.get(k).getFeatureofinterestid());
            } else if (featureofinterestList.get(k).getFeatureofinteresttypeid() == FeatureTypeEnum.CROP.getValue()) {
                ///CROP
                obj1.put("id", String.valueOf(featureofinterestList.get(k).getFeatureofinterestid()));
                obj1.put("identifier", featureofinterestList.get(k).getIdentifier());
                obj1.put("description", featureofinterestList.get(k).getName());
                obj1.put("featureType", FeatureTypeEnum.CROP.getValue());
            } else if (featureofinterestList.get(k).getFeatureofinteresttypeid() == FeatureTypeEnum.STATION.getValue()) {
                JSONObject obj2 = new JSONObject();
                //STATION
                obj2.put("id", String.valueOf(featureofinterestList.get(k).getFeatureofinterestid()));
                obj2.put("identifier", featureofinterestList.get(k).getIdentifier());
                obj2.put("description", featureofinterestList.get(k).getName());
                obj1.put("featureType", FeatureTypeEnum.STATION.getValue());
                list.add(obj2);
            }
        }

        List<CropInfoDTO> cropInfoList = featureofinterestJpaRepository.getFeatureByIds(featureids);
        List<String> tempList = new ArrayList<String>();


        //END DEVICE
        JSONArray finallist = new JSONArray();
        for (CropInfoDTO cropInfoDTO : cropInfoList) {
            tempList.add(String.valueOf((cropInfoDTO.getFeatureIdentifier())));
        }

        HashSet hs = new HashSet(tempList);
        tempList.clear();
        tempList.addAll(hs);

        for (int j = 0; j < tempList.size(); j++) {
            JSONObject finalobject = new JSONObject();
            JSONArray list2 = new JSONArray();

            int tmcounter = 0;
            for (CropInfoDTO cropInfoDTO : cropInfoList) {
                if (tempList.get(j).equals(String.valueOf((cropInfoDTO.getFeatureIdentifier())))) {
                    JSONObject tempobj = new JSONObject();
                    tempobj.put("kindofmeasurement", String.valueOf((cropInfoDTO.getObservableIdentifier())));
                    tempobj.put("sensorname", String.valueOf((cropInfoDTO.getProcedureIdentifier())));
                    tempobj.put("typeofmeasurement", String.valueOf((cropInfoDTO.getProcedureDescription())));
                    list2.add(tempobj);

                    if (tmcounter == 0) {
                        finalobject.put("description", String.valueOf((cropInfoDTO.getFeatureName())));
                        finalobject.put("id", String.valueOf(cropInfoDTO.getId()));
                        finalobject.put("featureType", FeatureTypeEnum.END_DEVICE.getValue());
                    }
                    tmcounter++;
                }


            }

            finalobject.put("sensors", list2);
            finalobject.put("identifier", tempList.get(j));
            finallist.add(finalobject);
        }


        obj.put("crop", obj1);
        obj.put("stations", list);
        obj.put("devices", finallist);

        //String temp = obj.toJSONString();
        return obj;
    }

    public List<FeatureidIdentifier> findByUserAndType(int id, long typeId) {
        List<FeatureidIdentifier> results = featureofinterestJpaRepository.getIdentifiers(id, typeId);
        return results;
    }

    public String findByIdentifier(String coordinator) {
        String jsonresult = null;

        try {
            List<String> results = featureofinterestJpaRepository.findEndDevicesByCoord(coordinator);
            ObjectMapper mapper = new ObjectMapper();
            jsonresult = mapper.writeValueAsString(results);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonresult;
    }

    public List<FeatureObsPropMinMax> findminmaxObservationValues(String identifier) {
        List<FeatureObsPropMinMax> results = null;
        try {
            results = featureofinterestJpaRepository.findChildMiMaxValuesByIdentifier(identifier);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        return results;

    }

    public String findMinMaxbyUserId(Integer userid) {
        String jsonInString = null;
        try {
            List<FeatureObsPropMinMax> results = featureofinterestJpaRepository.findFeatureMiMaxValuesByUserId(userid);

            List<FeatureObsProp> featureobsPropList = new ArrayList<FeatureObsProp>();
            featureobsPropList.add(new FeatureObsProp(results.get(0).getFeatureofinterestid(), results.get(0).getIdentifier(), results.get(0).getName()));

            for (FeatureObsPropMinMax obj : results) {
                boolean newaddition = true;
                for (int i = 0; i < featureobsPropList.size(); i++) {
                    FeatureObsProp feature = featureobsPropList.get(i);
                    if (obj.getFeatureofinterestid() == feature.getFeatureofinterestid()) {
                        newaddition = false;
                    }
                }

                if (newaddition) {
                    featureobsPropList.add(new FeatureObsProp(obj.getFeatureofinterestid(), obj.getIdentifier(), obj.getName()));
                }
            }

            for (FeatureObsPropMinMax obj : results) {
                for (FeatureObsProp feature : featureobsPropList) {
                    if (obj.getFeatureofinterestid() == feature.getFeatureofinterestid()) {
                        feature.getFeatureObsproplist().add(new FeatureMinMaxValue((obj.getObspropvalId()).longValue(), obj.getObspropertName(), obj.getMinval(), obj.getMaxval()));
                    }
                }
            }

            //Object to JSON in String
            ObjectMapper mapper = new ObjectMapper();
            jsonInString = mapper.writeValueAsString(featureobsPropList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonInString;

    }

    public String findByFeatureofinterestid(int id) {
        ObjectMapper mapper = new ObjectMapper();

        //Object to JSON in String
        String jsonInString = null;
        int key = 2;
        try {
            Featureofinterest featureofinterest = featureofinterestJpaRepository.getFeatureofinterestByFeatureofinterestid(key);
            List<Double> coords = Arrays.asList(featureofinterest.getGeom().getX(), featureofinterest.getGeom().getY());
            jsonInString = mapper.writeValueAsString(coords);
            //jsonInString = mapper.writeValueAsString(featureofinterest.getGeom().toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonInString;
    }

    public String findFeatureByIdentifier(String identifier) {
        String jsonRes = null;

        try {
            List<Object[]> objects = featureofinterestJpaRepository.findDatesByIdentifier(identifier);


            if (objects.size() == 0)
                return jsonRes;

            Timestamp timestampFrom = (java.sql.Timestamp) objects.get(0)[0];
            Timestamp timestampTo = (java.sql.Timestamp) objects.get(0)[1];

            DateTime dtFROM = new DateTime(timestampFrom.getTime());
            DateTime dtTO = new DateTime(timestampTo.getTime());

            JSONObject Setup = new JSONObject();
            Setup.put("frHours", dtFROM.getHourOfDay());
            Setup.put("frMinutes", dtFROM.getMinuteOfHour());
            Setup.put("frSeconds", dtFROM.getSecondOfDay());
            Setup.put("toHours", dtTO.getHourOfDay());
            Setup.put("toMinutes", dtTO.getMinuteOfHour());
            Setup.put("toSeconds", dtTO.getSecondOfDay());

            jsonRes = Setup.toJSONString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return jsonRes;
        }
    }

    public Featureofinterest getFeatureofinterestByIdentifier(String identifier) {
        try {
            Featureofinterest featureofinterest = featureofinterestJpaRepository.getFeatureofinterestByIdentifier(identifier);
            return featureofinterest;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<FeatureidIdentifier> findFeatureIdByIdentifier(List<String> idStr) {
        try {
            List<FeatureidIdentifier> list = featureofinterestJpaRepository.getIdidentif(idStr);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Long> findIdsByIdentifier(List<String> idStr) {
        try {
            List<Long> list = featureofinterestJpaRepository.getFeatureOfInterestId(idStr);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> findEndDevicesIdentifiers(String enddeviceIdentifier) {
        try {
            List<String> identiifers = featureofinterestJpaRepository.getEndDeviceIdentifiersByEndDeviceIdentifier(enddeviceIdentifier);
            return identiifers;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<EmebddedSetupDevicdeDto> findEndDevicesTimes(String coordinatorAddress) {
        try {
            List<Object[]> list = featureofinterestJpaRepository.getEnddevicesTimes(coordinatorAddress);
            List<EmebddedSetupDevicdeDto> endDeviceList = new ArrayList<EmebddedSetupDevicdeDto>();
            //TimeZone
            Featureofinterest featureofinterest = this.getFeatureofinterestByIdentifier(coordinatorAddress);
            DateTimeZone tz = DateTimeZone.forID(featureofinterest.getTimezone());

            //Convert time to UTC
            int offsetHours = tz.getOffset(new DateTime()) / (60 * 60 * 1000);
            for (Object[] obj : list) {

                Integer fromhour = Integer.parseInt(obj[1].toString()) + offsetHours;
                if (fromhour >= 24) {
                    fromhour = fromhour % 24;
                } else if(fromhour < 0) {
                    fromhour = 24 + fromhour;
                }

                Integer untilhour = Integer.parseInt(obj[3].toString()) + offsetHours;
                if (untilhour >= 24) {
                    untilhour = untilhour % 24;
                } else if(untilhour < 0) {
                    untilhour  = 24 + untilhour ;
                }
                endDeviceList.add(new EmebddedSetupDevicdeDto(obj[0].toString(), fromhour, Integer.parseInt(obj[2].toString()), untilhour, Integer.parseInt(obj[4].toString())));
            }

            return endDeviceList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public EmebddedSetupDevicdeDto findCoordinatorTimes(String coordinatorAddress) {
        try {
            List<Object[]> list = featureofinterestJpaRepository.getCoordinatorTimes(coordinatorAddress);
            if (list.size() == 0)
                return null;

            //TimeZone
            Featureofinterest featureofinterest = this.getFeatureofinterestByIdentifier(coordinatorAddress);
            DateTimeZone tz = DateTimeZone.forID(featureofinterest.getTimezone());

            //Convert time to UTC
            int offsetHours = tz.getOffset(new DateTime()) / (60 * 60 * 1000);
            Integer fromhour = Integer.parseInt(list.get(0)[1].toString()) + offsetHours;
            if (fromhour >= 24) {
                fromhour = fromhour % 24;
            } else if(fromhour < 0) {
                fromhour = 24 + fromhour;
            }

            Integer untilhour = Integer.parseInt(list.get(0)[3].toString()) + offsetHours;
            if (untilhour >= 24) {
                untilhour = untilhour % 24;
            } else if(untilhour < 0) {
                untilhour  = 24 + untilhour ;
            }

            EmebddedSetupDevicdeDto coordinatorTimes = new EmebddedSetupDevicdeDto(
                    coordinatorAddress, fromhour, Integer.parseInt(list.get(0)[2].toString()), untilhour, Integer.parseInt(list.get(0)[4].toString()));

            return coordinatorTimes;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public Long findseries(int obs, Integer fid) {
        Long returnedL = null;

        try {
            Long obsg = Long.valueOf(obs);
            Long fidg = Long.valueOf(fid.longValue());

            List<Long> temOb = featureofinterestJpaRepository.getAllSeriesId(fidg, obsg);
            if (temOb.size() > 0)
                returnedL = temOb.get(0);
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            return returnedL;
        }
    }


    public String findIrrigationAndMeasuring(String coordinator) {
        String jsonresult = null;

        try {
            Featureofinterest featureofinterest = this.getFeatureofinterestByIdentifier(coordinator);
            //
            Integer idCord = null;
            List<Integer> cordinIds = featureofinterestJpaRepository.getIdbyIdent(coordinator);

            Iterator itr = cordinIds.iterator();
            while (itr.hasNext()) {
                idCord = (Integer) itr.next();
            }

            Long fidg = Long.valueOf(idCord.longValue());
            List<EndDeviceStatusDTO> identiFlagsObjects = featureofinterestJpaRepository.getIdentifierFlags(fidg);

            //TimeZone
            DateTimeZone tz = DateTimeZone.forID(featureofinterest.getTimezone());
            int offset = tz.getOffset(new Instant());

            for (EndDeviceStatusDTO endDevice : identiFlagsObjects) {
                Calendar cal = Calendar.getInstance();

                cal.setTimeInMillis(endDevice.getFromtime().getTime());
                cal.add(Calendar.MILLISECOND, offset);
                endDevice.setFromtime(new Timestamp(cal.getTime().getTime()));

                cal.setTimeInMillis(endDevice.getUntiltime().getTime());
                cal.add(Calendar.MILLISECOND, offset);
                endDevice.setUntiltime(new Timestamp(cal.getTime().getTime()));
            }

            ObjectMapper mapper = new ObjectMapper();
            jsonresult = mapper.writeValueAsString(identiFlagsObjects);
        } catch (JsonProcessingException exc) {
            exc.printStackTrace();
        } finally {
            return jsonresult;
        }
    }

    public boolean changeMeasuringFlag(String identifier, long typeId) {
        try {
            List<String> endDeviceIdentifiers = featureofinterestJpaRepository.getEndDeviceIdentifiersByStation(identifier);
            if(endDeviceIdentifiers.size() == 0){
                throw new Exception("Station's identifier number is wrong");
            }
            featureofinterestJpaRepository.setMeasuringFlag(endDeviceIdentifiers, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public AutomaticWater getAutomaticWater(int userid, String identifier) {
        AutomaticWater automaticWater = null;

        try {
            Featureofinterest featureofinterest = this.getFeatureofinterestByIdentifier(identifier);
            //
            automaticWater = featureofinterestJpaRepository.getAutomaticWaterByEndDevice(userid, identifier);
            //
            DateTimeFormatter dtfInput = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            HelperCls.ConvertToDateTime convertable = new HelperCls.ConvertToDateTime();
            DateTime irrigdtFrom = convertable.GetUTCDateTime(automaticWater.getFromtime(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_TIMEZONE);
            automaticWater.setFromtime(dtfInput.print(irrigdtFrom));
            DateTime irrigdtUntil = convertable.GetUTCDateTime(automaticWater.getUntiltime(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_TIMEZONE);
            automaticWater.setUntiltime(dtfInput.print(irrigdtUntil));

            return automaticWater;
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            return automaticWater;
        }
    }

    public boolean setDeviceIrrigaDate(int usid, String device, String from, String to) {
        try {
            Featureofinterest featureofinterest = this.getFeatureofinterestByIdentifier(device);
            //
            DateTimeFormatter dtfInput = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            HelperCls.ConvertToDateTime convertable = new HelperCls.ConvertToDateTime();
            DateTime irrigdtFrom = convertable.GetUTCDateTime(from, dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_UTC);
            DateTime irrigdtUntil = convertable.GetUTCDateTime(to, dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_UTC);
            //
            featureofinterestJpaRepository.setDeviceIrrigDates(usid, device, new Timestamp(irrigdtFrom.getMillis()), new Timestamp(irrigdtUntil.getMillis()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setFeatureMeasuringFalse(List<String> idertifierList) {
        try {
            featureofinterestJpaRepository.setMeasuringFlag(idertifierList, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFeatureWateringFalse(String identifier) {
        try {
            featureofinterestJpaRepository.setWateringFlag(identifier, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAutomaticWateringTime(AutomaticWater automaticWater, int userid) {
        try {

            DateTimeFormatter dtfInput = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

            //Convert time to UTC
            Featureofinterest featureofinterest = this.getFeatureofinterestByIdentifier(automaticWater.getIdentifier());
            HelperCls.ConvertToDateTime convertable = new HelperCls.ConvertToDateTime();
            DateTime irrigdtFrom = convertable.GetUTCDateTime(automaticWater.getFromtime(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_UTC);
            DateTime irrigdtUntil = convertable.GetUTCDateTime(automaticWater.getUntiltime(), dtfInput, featureofinterest.getTimezone(), StatusTimeConverterEnum.TO_UTC);

            featureofinterestJpaRepository.setCoordinatorAlgorithmParams(automaticWater.getIdentifier(), new Timestamp(irrigdtFrom.getMillis()), new Timestamp(irrigdtUntil.getMillis()), automaticWater.getWateringConsumption());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFeatureOfInterestData(FeatureSensor featureSensor) {
        try {
            featureofinterestJpaRepository.setFeatureOfInterestData(featureSensor.getIdentifier(), featureSensor.getDescription(), featureSensor.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}