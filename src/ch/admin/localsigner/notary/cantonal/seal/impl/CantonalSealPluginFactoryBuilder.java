/*
 * Copyright 2020 The Federal Authorities of the Swiss Confederation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.admin.localsigner.notary.cantonal.seal.impl;

import ch.glue.localsigner.cantonal.seal.factory.PluginInstantiationException;
import ch.glue.localsigner.cantonal.seal.factory.AbstractCantonalSealPluginFactory;
import org.apache.log4j.Logger;
import ch.glue.localsigner.cantonal.seal.configuration.transfer.EndPoints;
import ch.glue.localsigner.cantonal.seal.impl.sdms.SdmsSealPluginFactory;

/**
 * Creates the plugin according to the configuration in the endpoints xml.
 *
 * @author greiler
 */
public class CantonalSealPluginFactoryBuilder
{
  private final static Logger LOGGER = Logger.getLogger(CantonalSealPluginFactoryBuilder.class);

  /* canton check deactivated, see LOCALSIG-364
  @AllArgsConstructor
  @Getter
  private enum Canton {
    VD("vd"),
    GE("ge");
    private String abbrev;
  }*/

  private enum Domain {
    NOTARY("upreg");
    private String identifier;

    private Domain(String identifier)
    {
      this.identifier = identifier;
    }

    public String getIdentifier()
    {
      return identifier;
    }
  }

  public static AbstractCantonalSealPluginFactory build(String canton, String domain)
      throws PluginInstantiationException
  {
    if (existsPluginFactory(canton, domain))
    { // existsPluginFactory verwendet, da nur momentan dieses Plugin verfügbar. Zukünftig dann spezifisch pro Factory.
      return new SdmsSealPluginFactory(canton, domain);
    }

    String msg = "There is no plugin factory implemented for domain " + domain
        + " and canton " + canton;
    LOGGER.error(msg);

    throw new PluginInstantiationException(
        PluginInstantiationException.PluginInstantiationErrorCode.ERR_NO_MATCHING_FACTORY_FOR_DOMAIN_AND_CANTON, msg);
  }

  public static boolean existsPluginFactory(String canton, String domain)
  {
    if (domain.equalsIgnoreCase(Domain.NOTARY.getIdentifier())
        && isSupportedCanton(canton))
    {
      LOGGER.debug("There is a plugin for canton " + canton + " in domain " + domain);
      return true;
    }

    LOGGER.debug("There is no plugin for canton " + canton + " in domain " + domain);
    return false;
  }

  private static boolean isSupportedCanton(String canton)
  {
    // canton check deactivated, see LOCALSIG-364
    // return canton.equalsIgnoreCase(Canton.VD.getAbbrev()) || canton.equalsIgnoreCase(Canton.GE.getAbbrev());
    return true;
  }

  public static boolean isSealConfigured(String canton, String domain, EndPoints configuredEndpoints)
  {
    for (EndPoints.EndPoint configuredEndPoint : configuredEndpoints.getEndPoint())
    {
      if (canton.equalsIgnoreCase(configuredEndPoint.getCanton()) &&
          domain.equalsIgnoreCase(configuredEndPoint.getDomain()))
      {
        return true;
      }
    }

    return false;
  }
}