package br.inova.mobile.task;

import java.util.Arrays;
import java.util.Date;

import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.form.Form;
import br.inova.mobile.photo.Photo;
import br.inova.mobile.photo.PhotoDao;

import com.j256.ormlite.dao.CloseableIterator;

public class TaskTestsGenerator {
        
        public static void createRegisters() {
                CloseableIterator<Task> taskIterator = TaskDao.getIteratorForUnfinishedTasks();
                
                try {
                        Task baseTask = getBaseTask();
                        Photo basePhoto = getBasePhoto();
                        
                        if (baseTask != null && basePhoto != null) {
                                while (taskIterator.hasNext()) {
                                        Task iterateTask = (Task) taskIterator.next();
                                        createTask(iterateTask, baseTask, basePhoto);
                                }
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        taskIterator.closeQuietly();
                }
        }
        
        private static void verifyRegisters() {
                CloseableIterator<Task> iterator = TaskDao.getIteratorForAllTasksForCurrentUser();
                
                try {
                        while (iterator.hasNext()) {
                                Task task = (Task) iterator.next();
                                
                                if (!task.isDone()) {
                                        task.isDone();
                                }
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        iterator.closeQuietly();
                }
        }
        
        private static Task getBaseTask() {
                CloseableIterator<Task> taskIterator = TaskDao.getIteratorForFinishedTasks();
                Task baseTask = null;
                
                try {
                        while (taskIterator.hasNext()) {
                                baseTask = (Task) taskIterator.next();
                                break;
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        taskIterator.closeQuietly();
                }
                
                return baseTask;
        }
        
        private static Photo getBasePhoto() {
                CloseableIterator<Photo> photoIterator = PhotoDao.getIteratorForAllPhotos();
                Photo basePhoto = null;
                
                try {
                        while (photoIterator.hasNext()) {
                                basePhoto = (Photo) photoIterator.next();
                                break;
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        photoIterator.closeQuietly();
                }
                
                return basePhoto;
        }
        
        private static void createTask(
                                       Task iterateTask,
                                       Task baseTask,
                                       Photo basePhoto) {
                
                try {
                        iterateTask.setDone(true);
                        
                        Form iterateForm = iterateTask.getForm();
                        Form baseForm = baseTask.getForm();
                        
                        iterateForm.setAsphaltGuide(baseForm.getAsphaltGuide());
                        iterateForm.setDate(new Date());
                        iterateForm.setEnergy(baseForm.getEnergy());
                        iterateForm.setNumberConfirmation(baseForm.getNumberConfirmation());
                        iterateForm.setOtherNumbers(baseForm.getOtherNumbers());
                        iterateForm.setPavimentation(baseForm.getPavimentation());
                        iterateForm.setPluvialGallery(baseForm.getPluvialGallery());
                        iterateForm.setPrimaryUse(baseForm.getPrimaryUse());
                        iterateForm.setPublicIlumination(baseForm.getPublicIlumination());
                        iterateForm.setSecondaryUse(baseForm.getSecondaryUse());
                        iterateForm.setVariance(baseForm.getVariance());
                        
                        Photo replicatedPhoto = new Photo();
                        replicatedPhoto.setBase64(basePhoto.getBase64());
                        replicatedPhoto.setForm(iterateForm);
                        replicatedPhoto.setPath(basePhoto.getPath());
                        
                        Photo replicatedPhoto2 = new Photo();
                        replicatedPhoto2.setBase64(basePhoto.getBase64());
                        replicatedPhoto2.setForm(iterateForm);
                        replicatedPhoto2.setPath(basePhoto.getPath());
                        
                        PhotoDao.savePhotos(Arrays.asList(replicatedPhoto, replicatedPhoto2));
                        TaskDao.updateTask(iterateTask);
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                
        }
        
}
