import com.amazonaws.auth.BasicAWSCredentials;
 x

import com.amazonaws.services.support.model.DescribeTrustedAdvisorCheckResultRequest;
import com.amazonaws.services.support.model.DescribeTrustedAdvisorCheckResultResult;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.*;

@Tags({"aws", "learning", "processor"})
@CapabilityDescription("processing Ec2")
public class TrustedAdvisor extends Abstract Processor {
    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;


    final String checkId = result.getChecks().get(0).getId();
    final String checkName = result.getChecks().get(0).getName();

    public static final PropertyDescriptor SecretKey = new PropertyDescriptor.Builder()
            .name("AWSSecret")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    public static final PropertyDescriptor Key = new PropertyDescriptor.Builder()
            .name("AwsKey")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    public static final PropertyDescriptor Region = new PropertyDescriptor.Builder()
            .name("AWS Region")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("SUCCESS RELATIONSHIP")
            .build();


    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
        propertyDescriptors.add(SecretKey);
        propertyDescriptors.add(Key);
        propertyDescriptors.add(Region);
        this.properties = Collections.unmodifiableList(propertyDescriptors);

        Set<Relationship> relationships = new HashSet<>();
        relationships.add(SUCCESS);
        this.relationships = Collections.unmodifiableSet(relationships);
    }


    System.out.println("The following check is being refreshed: "+
    checkId +":"+"  "+checkName);

    DescribeTrustedAdvisorCheckResultRequest checkResultRequest =
            new DescribeTrustedAdvisorCheckResultRequest().withCheckId(checkId);
    DescribeTrustedAdvisorCheckResultResult checkResult =
            client.describeTrustedAdvisorCheckResult(checkResultRequest);

    @Override
    public void onTrigger(ProcessContext processContext, ProcessSession processSession) throws ProcessException {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(processContext.getProperty(Key).getValue(), processContext.getProperty(SecretKey).getValue());

    }
}