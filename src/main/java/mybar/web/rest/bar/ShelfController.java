package mybar.web.rest.bar;

import lombok.extern.slf4j.Slf4j;
import mybar.api.bar.IBottle;
import mybar.app.RestBeanConverter;
import mybar.app.bean.bar.BottleBean;
import mybar.app.bean.bar.View;
import mybar.service.bar.ShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shelf")
@Slf4j
public class ShelfController {

    private final ShelfService shelfService;

    @Autowired
    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    //-------------------Retrieve All Bottles--------------------------------------------------------

    @GetMapping("/bottles")
    public ResponseEntity<MappingJacksonValue> listAllBottles() {

        List<IBottle> allBottles = shelfService.findAllBottles();

        MappingJacksonValue wrapper = new MappingJacksonValue(toRestModels(allBottles));
        wrapper.setSerializationView(View.Shelf.class);

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    private static List<BottleBean> toRestModels(List<IBottle> allBottles) {
        return allBottles
                .stream()
                .map(RestBeanConverter::from)
                .collect(Collectors.toList());
    }

    //-------------------Retrieve a Bottle--------------------------------------------------------

    @GetMapping("/bottles/{id}")
    public ResponseEntity<BottleBean> getBottle(@PathVariable("id") String id) {
        log.info("Fetching a bottle with id = {}", id);

        IBottle bottle = shelfService.findById(id);
        return new ResponseEntity<>(RestBeanConverter.from(bottle), HttpStatus.OK);
    }

    //-------------------Create a Bottle--------------------------------------------------------

    @PostMapping("/bottles")
    public ResponseEntity<BottleBean> createBottle(@RequestBody BottleBean bottleBean, UriComponentsBuilder ucBuilder) {
        log.info("Adding a new bottle = {}", bottleBean);

        IBottle saved = shelfService.saveBottle(bottleBean);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/bottle/{id}").buildAndExpand(bottleBean.getId()).toUri());
        return new ResponseEntity<>(RestBeanConverter.from(saved), headers, HttpStatus.CREATED);
    }

    //------------------- Update a Bottle --------------------------------------------------------

    @PutMapping("/bottles")
    public ResponseEntity<BottleBean> updateBottle(@RequestBody BottleBean bottleBean) {
        log.info("Updating a bottle = {}", bottleBean);

        final IBottle updated = shelfService.updateBottle(bottleBean);
        return new ResponseEntity<>(RestBeanConverter.from(updated), HttpStatus.ACCEPTED);
    }

    //------------------- Delete a Bottle --------------------------------------------------------

    @DeleteMapping("/bottles/{id}")
    public ResponseEntity<BottleBean> deleteBottle(@PathVariable("id") String id) {
        log.info("Deleting a bottle with id = {}", id);

        shelfService.deleteBottleById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //------------------- Delete All Bottles --------------------------------------------------------

    @DeleteMapping("/bottles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BottleBean> deleteAllBottles() {
        log.info("Deleting all bottles");

        shelfService.deleteAllBottles();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}